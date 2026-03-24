package com.training.bad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point - demonstrates the bad OrderProcessor with 3 scenarios.
 * Run with: mvn exec:java
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        log.info("==============================================");
        log.info("  BAD OrderProcessor - Day 4 Training Demo  ");
        log.info("==============================================");

        // -------------------------------------------------------
        // Scenario 1: Credit card order - Alice (250 loyalty pts)
        // Total: 2x Mouse ($29.99) + 1x Keyboard ($79.99) = $139.97
        // Discount: >= $100 threshold → 5%
        // Loyalty: 250pts → up to 15% loyalty discount applied
        // -------------------------------------------------------
        log.info("\n--- Scenario 1: Credit Card Order (Alice) ---");
        OrderProcessor op1 = OrderProcessor.getInstance();
        op1.setCustomer("Alice Smith", "alice@example.com", "123 Main St, Springfield, NY", 1);
        op1.addItem("Mouse",    2, 29.99);
        op1.addItem("Keyboard", 1, 79.99);
        op1.setCard("4111111111111111", "12/26", "123");
        boolean result1 = op1.processOrder();
        log.info("Scenario 1 result: {} | Status: {}", result1 ? "SUCCESS" : "FAILED", op1.getStatus());

        // Reset the dangerous singleton state for next order
        op1.reset();

        // -------------------------------------------------------
        // Scenario 2: PayPal order - Bob (50 loyalty pts, no loyalty discount)
        // Total: 1x USB Hub ($19.99) - below all discount thresholds
        // -------------------------------------------------------
        log.info("\n--- Scenario 2: PayPal Order (Bob) ---");
        op1.setCustomer("Bob Jones", "bob@example.com", "456 Oak Ave, Houston, TX", 2);
        op1.addItem("USB Hub", 2, 19.99);
        op1.setPaypal("bob@example.com");
        boolean result2 = op1.processOrder();
        log.info("Scenario 2 result: {} | Status: {}", result2 ? "SUCCESS" : "FAILED", op1.getStatus());

        op1.reset();

        // -------------------------------------------------------
        // Scenario 3: Bank transfer - large order triggers 20% discount
        // Total: 1x Laptop ($999.99) + 1x Monitor ($399.99) = $1399.98
        // Discount: >= $1000 threshold → 20%
        // -------------------------------------------------------
        log.info("\n--- Scenario 3: Bank Transfer Order (Alice, large order) ---");
        op1.setCustomer("Alice Smith", "alice@example.com", "123 Main St, Springfield, NY", 1);
        op1.addItem("Laptop",  1, 999.99);
        op1.addItem("Monitor", 1, 399.99);
        op1.setBankTransfer("12345678", "123456789");
        boolean result3 = op1.processOrder();
        log.info("Scenario 3 result: {} | Status: {}", result3 ? "SUCCESS" : "FAILED", op1.getStatus());

        op1.reset();

        // -------------------------------------------------------
        // Scenario 4: Validation failures - shows bad error handling
        // -------------------------------------------------------
        log.info("\n--- Scenario 4: Validation Failures ---");

        // 4a: Product not in DB
        log.info("  [4a] Adding non-existent product:");
        op1.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op1.addItem("Hoverboard", 1, 299.99);
        log.info("  Items after bad add: {}", op1.getT() == 0 ? "empty (correct)" : "has items (bad!)");

        // 4b: Insufficient stock
        log.info("  [4b] Ordering more than stock (Monitor has 5, requesting 10):");
        op1.addItem("Monitor", 10, 399.99);
        log.info("  Total after: ${}", op1.getT());

        // 4c: Already processed double-submit
        log.info("  [4c] Double-submit on processed order:");
        op1.addItem("Mouse", 1, 29.99);
        op1.setCard("4111111111111111", "12/26", "123");
        op1.processOrder(); // first call ok
        boolean duplicate = op1.processOrder(); // second call should be rejected
        log.info("  Double submit result: {}", duplicate ? "PROCESSED (bad!)" : "REJECTED (correct)");

        op1.reset();

        // -------------------------------------------------------
        // Scenario 5: Declined payment
        // -------------------------------------------------------
        log.info("\n--- Scenario 5: Declined Credit Card ---");
        op1.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op1.addItem("Mouse", 1, 29.99);
        op1.setCard("1234567890000000", "12/26", "999"); // ends in 0000 = declined
        boolean result5 = op1.processOrder();
        log.info("Scenario 5 result: {}", result5 ? "PROCESSED (bad!)" : "DECLINED (correct)");

        log.info("\n==============================================");
        log.info("  Total orders processed: {}", OrderProcessor.getCnt());
        log.info("  Check invoice_*.txt files in project root");
        log.info("  Check orders.log for the audit trail");
        log.info("==============================================");
    }
}
