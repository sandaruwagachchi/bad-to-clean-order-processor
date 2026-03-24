package com.training.bad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * TODO: fix this later
 * written by: bob
 * date: sometime last year
 *
 * NOTE FOR TRAINING: MySQL replaced with H2 in-memory DB so this runs without
 * any external infrastructure. All SOLID violations and code smells are intact.
 * HTTP calls are stubbed via FakePaymentGateway (same smell: tight coupling).
 */
public class OrderProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);

    // stuff we need
    private static OrderProcessor inst;
    private Connection c;
    // H2 in-memory DB replaces MySQL - all the same bad practices remain
    private String dbUrl = "jdbc:h2:mem:shop;DB_CLOSE_DELAY=-1";
    private String u = "sa";
    private String p = "";
    private double tx = 0.08;
    private double disc1 = 0.05;
    private double disc2 = 0.10;
    private double disc3 = 0.20;
    private int t1 = 100;
    private int t2 = 500;
    private int t3 = 1000;
    private String apiKey = "sk-abc123secret";
    private String smtpHost = "smtp.company.com";
    private int smtpPort = 587;
    private String emailUser = "orders@company.com";
    private String emailPass = "emailpass99";
    private List<Map<String, Object>> items = new ArrayList<>();
    private double total = 0;
    private String custName;
    private String custEmail;
    private String custAddr;
    private int custId;
    private String cardNum;
    private String cardExp;
    private String cardCvv;
    private String paypalEmail;
    private String bankAcc;
    private String bankRouting;
    private String paymentType;
    private boolean processed = false;
    private static int orderCount = 0;

    // private constructor - singleton
    private OrderProcessor() {
        try {
            c = DriverManager.getConnection(dbUrl, u, p);
            initSchema();
            seedData();
        } catch (Exception e) {
            log.error("db error: {}", e.getMessage());
        }
    }

    // Schema init - in real bad code this would be in a migration tool or not exist at all
    private void initSchema() throws SQLException {
        Statement st = c.createStatement();
        st.execute("""
            CREATE TABLE IF NOT EXISTS customers (
                id INT PRIMARY KEY,
                name VARCHAR(100),
                email VARCHAR(100),
                loyalty_points INT DEFAULT 0
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS products (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) UNIQUE,
                stock INT DEFAULT 0,
                price DOUBLE
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id INT AUTO_INCREMENT PRIMARY KEY,
                customer_id INT,
                total DOUBLE,
                discount DOUBLE,
                tax DOUBLE,
                status VARCHAR(20),
                tx_id VARCHAR(50),
                created_at TIMESTAMP
            )""");
        st.execute("""
            CREATE TABLE IF NOT EXISTS order_items (
                id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT,
                product_name VARCHAR(100),
                qty INT,
                price DOUBLE
            )""");
        st.close();
    }

    // Seed some test data
    private void seedData() throws SQLException {
        Statement st = c.createStatement();
        // customers
        st.execute("MERGE INTO customers KEY(id) VALUES (1, 'Alice Smith', 'alice@example.com', 250)");
        st.execute("MERGE INTO customers KEY(id) VALUES (2, 'Bob Jones',   'bob@example.com',   50)");
        // products
        st.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Laptop',     10, 999.99)");
        st.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Mouse',      50,  29.99)");
        st.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Keyboard',   30,  79.99)");
        st.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Monitor',     5, 399.99)");
        st.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'USB Hub',    20,  19.99)");
        st.close();
    }

    public static OrderProcessor getInstance() {
        if (inst == null) {
            inst = new OrderProcessor();
        }
        return inst;
    }

    // add item to order
    public void addItem(String nm, int qty, double pr) {
        if (nm == null || nm.equals("")) {
            log.warn("bad name");
            return;
        }
        if (qty <= 0) {
            log.warn("bad qty");
            return;
        }
        if (pr < 0) {
            log.warn("bad price");
            return;
        }
        // check stock in db
        try {
            PreparedStatement ps = c.prepareStatement("SELECT stock FROM products WHERE name=?");
            ps.setString(1, nm);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stk = rs.getInt("stock");
                if (stk < qty) {
                    log.warn("not enough stock for {}, available: {}", nm, stk);
                    return;
                }
            } else {
                log.warn("product not found: {}", nm);
                return;
            }
        } catch (Exception e) {
            log.error("stock check error: {}", e.getMessage());
            return;
        }
        Map<String, Object> item = new HashMap<>();
        item.put("name", nm);
        item.put("qty", qty);
        item.put("price", pr);
        item.put("subtotal", qty * pr);
        items.add(item);
        total += (double) item.get("subtotal");
    }

    public void setCustomer(String n, String e, String a, int id) {
        this.custName = n;
        this.custEmail = e;
        this.custAddr = a;
        this.custId = id;
    }

    public void setCard(String num, String exp, String cvv) {
        this.cardNum = num;
        this.cardExp = exp;
        this.cardCvv = cvv;
        this.paymentType = "CREDIT_CARD";
    }

    public void setPaypal(String email) {
        this.paypalEmail = email;
        this.paymentType = "PAYPAL";
    }

    public void setBankTransfer(String acc, String routing) {
        this.bankAcc = acc;
        this.bankRouting = routing;
        this.paymentType = "BANK_TRANSFER";
    }

    // THE BIG METHOD - processes everything
    public boolean processOrder() {

        if (processed) {
            log.warn("already processed");
            return false;
        }

        if (items.isEmpty()) {
            log.warn("no items");
            return false;
        }

        if (custName == null || custEmail == null) {
            log.warn("no customer");
            return false;
        }

        // calculate discount
        double disc = 0;
        if (total >= t3) {
            disc = disc3;
        } else if (total >= t2) {
            disc = disc2;
        } else if (total >= t1) {
            disc = disc1;
        }
        double discAmt = total * disc;
        double afterDisc = total - discAmt;

        // check loyalty points
        int loyaltyPts = 0;
        double loyaltyDisc = 0;
        try {
            PreparedStatement ps = c.prepareStatement("SELECT loyalty_points FROM customers WHERE id=?");
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loyaltyPts = rs.getInt("loyalty_points");
                if (loyaltyPts >= 100) {
                    loyaltyDisc = Math.min(loyaltyPts * 0.01, afterDisc * 0.15);
                    afterDisc -= loyaltyDisc;
                }
            }
        } catch (Exception e) {
            log.error("loyalty error: {}", e.getMessage());
        }

        // tax
        double taxAmt = afterDisc * tx;
        double finalTotal = afterDisc + taxAmt;

        // process payment
        boolean payOk = false;
        String txId = "";

        if ("CREDIT_CARD".equals(paymentType)) {
            // validate card
            if (cardNum == null || cardNum.length() != 16) {
                log.error("bad card number");
                return false;
            }
            if (cardCvv == null || cardCvv.length() < 3) {
                log.error("bad cvv");
                return false;
            }
            // stub replaces real HTTP call - same bad tight-coupling smell
            FakePaymentGateway.ChargeResult result =
                FakePaymentGateway.chargeCreditCard(apiKey, cardNum, cardExp, cardCvv, finalTotal);
            if (result.success()) {
                payOk = true;
                txId = "CC-" + System.currentTimeMillis();
                log.info("card charged ok");
            } else {
                log.error("card failed: {}", result.message());
                return false;
            }

        } else if ("PAYPAL".equals(paymentType)) {
            if (paypalEmail == null || !paypalEmail.contains("@")) {
                log.error("bad paypal email");
                return false;
            }
            FakePaymentGateway.ChargeResult result =
                FakePaymentGateway.chargePaypal(apiKey, paypalEmail, finalTotal);
            if (result.success()) {
                payOk = true;
                txId = "PP-" + System.currentTimeMillis();
                log.info("paypal ok");
            } else {
                log.error("paypal failed: {}", result.message());
                return false;
            }

        } else if ("BANK_TRANSFER".equals(paymentType)) {
            if (bankAcc == null || bankAcc.length() < 8) {
                log.error("bad bank account");
                return false;
            }
            if (bankRouting == null || bankRouting.length() != 9) {
                log.error("bad routing");
                return false;
            }
            FakePaymentGateway.ChargeResult result =
                FakePaymentGateway.chargeBankTransfer(apiKey, bankAcc, bankRouting, finalTotal);
            if (result.success()) {
                payOk = true;
                txId = "BT-" + System.currentTimeMillis();
                log.info("bank transfer ok");
            } else {
                log.error("bank failed: {}", result.message());
                return false;
            }
        } else {
            log.error("unknown payment type");
            return false;
        }

        // save order to db
        int orderId = -1;
        try {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO orders (customer_id, total, discount, tax, status, tx_id, created_at) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, custId);
            ps.setDouble(2, finalTotal);
            ps.setDouble(3, discAmt + loyaltyDisc);
            ps.setDouble(4, taxAmt);
            ps.setString(5, "PAID");
            ps.setString(6, txId);
            ps.setObject(7, LocalDateTime.now());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) orderId = rs.getInt(1);

            // save items
            for (Map<String, Object> item : items) {
                PreparedStatement ps2 = c.prepareStatement(
                        "INSERT INTO order_items (order_id, product_name, qty, price) VALUES (?,?,?,?)");
                ps2.setInt(1, orderId);
                ps2.setString(2, (String) item.get("name"));
                ps2.setInt(3, (int) item.get("qty"));
                ps2.setDouble(4, (double) item.get("price"));
                ps2.executeUpdate();
            }

            // update stock
            for (Map<String, Object> item : items) {
                PreparedStatement ps3 = c.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE name = ?");
                ps3.setInt(1, (int) item.get("qty"));
                ps3.setString(2, (String) item.get("name"));
                ps3.executeUpdate();
            }

            // update loyalty points
            int newPts = (int)(finalTotal / 10);
            int deducted = loyaltyPts >= 100 ? (int)(loyaltyDisc / 0.01) : 0;
            PreparedStatement ps4 = c.prepareStatement(
                    "UPDATE customers SET loyalty_points = loyalty_points + ? - ? WHERE id = ?");
            ps4.setInt(1, newPts);
            ps4.setInt(2, deducted);
            ps4.setInt(3, custId);
            ps4.executeUpdate();

        } catch (Exception e) {
            log.error("db save error: {}", e.getMessage());
            // payment already went through but we can't save - bad!!
            return false;
        }

        // send email (stubbed - just logs)
        try {
            String body = "Dear " + custName + ",\n\n" +
                    "Thank you for your order #" + orderId + "!\n\n" +
                    "Items:\n";
            for (Map<String, Object> item : items) {
                body += "  - " + item.get("name") + " x" + item.get("qty") +
                        " @ $" + item.get("price") + "\n";
            }
            body += "\nSubtotal: $" + total + "\n";
            body += "Discount: -$" + String.format("%.2f", discAmt + loyaltyDisc) + "\n";
            body += "Tax: $" + String.format("%.2f", taxAmt) + "\n";
            body += "Total: $" + String.format("%.2f", finalTotal) + "\n\n";
            body += "Payment Method: " + paymentType + "\n";
            body += "Transaction ID: " + txId + "\n\n";
            body += "Shipping to: " + custAddr + "\n\n";
            body += "Order will arrive in 3-5 business days.\n";
            log.info("Sending email to {}...\n{}", custEmail, body);

            // also log to file
            FileWriter fw = new FileWriter("orders.log", true);
            fw.write(LocalDateTime.now() + " | Order #" + orderId + " | " + custEmail +
                    " | $" + String.format("%.2f", finalTotal) + " | " + txId + "\n");
            fw.close();
        } catch (Exception e) {
            log.error("email error: {}", e.getMessage());
        }

        // generate invoice (fake)
        try {
            FileWriter fw = new FileWriter("invoice_" + orderId + ".txt");
            fw.write("INVOICE #" + orderId + "\n");
            fw.write("========================\n");
            fw.write("Customer: " + custName + "\n");
            fw.write("Address: " + custAddr + "\n");
            fw.write("Date: " + LocalDateTime.now() + "\n\n");
            for (Map<String, Object> item : items) {
                fw.write(item.get("name") + " x" + item.get("qty") +
                        " = $" + item.get("subtotal") + "\n");
            }
            fw.write("------------------------\n");
            fw.write("TOTAL: $" + String.format("%.2f", finalTotal) + "\n");
            fw.close();
            log.info("Invoice written to invoice_{}.txt", orderId);
        } catch (Exception e) {
            log.error("invoice error: {}", e.getMessage());
        }

        // update analytics (stubbed)
        FakePaymentGateway.sendAnalyticsEvent(orderId, finalTotal, custId);

        orderCount++;
        processed = true;
        log.info("Order #{} done. Total orders processed: {}", orderId, orderCount);
        return true;
    }

    public double getT() { return total; }
    public boolean isDone() { return processed; }
    public static int getCnt() { return orderCount; }

    // reset for next order - dangerous on singleton!
    public void reset() {
        items.clear();
        total = 0;
        custName = null; custEmail = null; custAddr = null; custId = 0;
        cardNum = null; cardExp = null; cardCvv = null;
        paypalEmail = null; bankAcc = null; bankRouting = null;
        paymentType = null; processed = false;
    }

    // helper - nobody uses this
    public double calcShipping(String addr) {
        if (addr.contains("NY") || addr.contains("CA")) {
            return 5.99;
        } else if (addr.contains("TX") || addr.contains("FL")) {
            return 7.99;
        } else {
            return 12.99;
        }
    }

    // duplicate of addItem almost
    public void addItemWithDiscount(String nm, int qty, double pr, double itemDisc) {
        if (nm == null || nm.equals("")) { log.warn("bad name"); return; }
        if (qty <= 0) { log.warn("bad qty"); return; }
        if (pr < 0) { log.warn("bad price"); return; }
        double discPrice = pr - (pr * itemDisc);
        Map<String, Object> item = new HashMap<>();
        item.put("name", nm);
        item.put("qty", qty);
        item.put("price", discPrice);
        item.put("subtotal", qty * discPrice);
        items.add(item);
        total += (double) item.get("subtotal");
        // note: no stock check here - oops
    }

    // not sure if this is still needed
    @Deprecated
    public void processCreditCardOrder() {
        if (!"CREDIT_CARD".equals(paymentType)) {
            log.warn("not a credit card order");
            return;
        }
        processOrder();
    }

    public String getStatus() {
        if (!processed) return "PENDING";
        return "DONE";
    }
}
