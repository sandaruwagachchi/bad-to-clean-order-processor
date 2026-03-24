package com.training.clean.repository;

import com.training.clean.model.Order;
import com.training.clean.model.OrderItem;
import com.training.clean.service.OrderPricing;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;

public class H2OrderRepository implements OrderRepository {

    private final Connection connection;

    public H2OrderRepository(String dbUrl, String user, String password) {
        try {
            this.connection = DriverManager.getConnection(dbUrl, user, password);
            initSchema();
            seedData();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to initialize repository", ex);
        }
    }

    public H2OrderRepository() {
        this("jdbc:h2:mem:shop-clean;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Override
    public Optional<CustomerSnapshot> findCustomer(int customerId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, email, loyalty_points FROM customers WHERE id=?")) {
            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new CustomerSnapshot(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("loyalty_points")
                ));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load customer", ex);
        }
    }

    @Override
    public OptionalInt findStock(String productName) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock FROM products WHERE name=?")) {
            statement.setString(1, productName);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(rs.getInt("stock"));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load stock", ex);
        }
    }

    @Override
    public int savePaidOrder(Order order,
                             OrderPricing pricing,
                             String transactionId,
                             int earnedLoyaltyPoints,
                             int redeemedLoyaltyPoints) {
        try {
            connection.setAutoCommit(false);
            int orderId = insertOrder(order, pricing, transactionId);
            insertOrderItems(orderId, order.items());
            updateStock(order.items());
            updateLoyalty(order.customer().id(), earnedLoyaltyPoints, redeemedLoyaltyPoints);
            connection.commit();
            return orderId;
        } catch (SQLException ex) {
            rollbackQuietly();
            throw new RuntimeException("Failed to save order", ex);
        } finally {
            resetAutoCommit();
        }
    }

    private int insertOrder(Order order, OrderPricing pricing, String transactionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO orders (customer_id, total, discount, tax, status, tx_id, created_at) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, order.customer().id());
            statement.setDouble(2, pricing.finalTotal());
            statement.setDouble(3, pricing.totalDiscount());
            statement.setDouble(4, pricing.taxAmount());
            statement.setString(5, "PAID");
            statement.setString(6, transactionId);
            statement.setObject(7, LocalDateTime.now());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to get order id");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertOrderItems(int orderId, Iterable<OrderItem> items) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO order_items (order_id, product_name, qty, price) VALUES (?,?,?,?)")) {
            for (OrderItem item : items) {
                statement.setInt(1, orderId);
                statement.setString(2, item.productName());
                statement.setInt(3, item.quantity());
                statement.setDouble(4, item.unitPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void updateStock(Iterable<OrderItem> items) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE products SET stock = stock - ? WHERE name = ?")) {
            for (OrderItem item : items) {
                statement.setInt(1, item.quantity());
                statement.setString(2, item.productName());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void updateLoyalty(int customerId, int earnedLoyaltyPoints, int redeemedLoyaltyPoints) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE customers SET loyalty_points = loyalty_points + ? - ? WHERE id = ?")) {
            statement.setInt(1, earnedLoyaltyPoints);
            statement.setInt(2, redeemedLoyaltyPoints);
            statement.setInt(3, customerId);
            statement.executeUpdate();
        }
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private void initSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INT PRIMARY KEY,
                        name VARCHAR(100),
                        email VARCHAR(100),
                        loyalty_points INT DEFAULT 0
                    )""");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) UNIQUE,
                        stock INT DEFAULT 0,
                        price DOUBLE
                    )""");
            statement.execute("""
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
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        order_id INT,
                        product_name VARCHAR(100),
                        qty INT,
                        price DOUBLE
                    )""");
        }
    }

    private void seedData() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("MERGE INTO customers KEY(id) VALUES (1, 'Alice Smith', 'alice@example.com', 250)");
            statement.execute("MERGE INTO customers KEY(id) VALUES (2, 'Bob Jones', 'bob@example.com', 50)");
            statement.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Laptop', 10, 999.99)");
            statement.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Mouse', 50, 29.99)");
            statement.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Keyboard', 30, 79.99)");
            statement.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'Monitor', 5, 399.99)");
            statement.execute("MERGE INTO products KEY(name) VALUES (DEFAULT, 'USB Hub', 20, 19.99)");
        }
    }
}

