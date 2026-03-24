package com.training.bad;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the bad OrderProcessor.
 *
 * NOTE FOR TRAINING: Notice how painful it is to test this class:
 * - We can't mock the database (no abstraction)
 * - We can't mock the payment gateway (tightly coupled to FakePaymentGateway)
 * - The Singleton makes test isolation impossible without reset()
 * - State leaks between tests if reset() is forgotten
 * This is exactly why DIP and SRP matter.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderProcessorTest {

    private OrderProcessor op;

    @BeforeEach
    void setUp() {
        op = OrderProcessor.getInstance();
        op.reset(); // dangerous - we depend on an internal reset method
    }

    @Test
    @Order(1)
    void creditCardOrder_shouldSucceed() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St, NY", 1);
        op.addItem("Mouse", 1, 29.99);
        op.setCard("4111111111111111", "12/26", "123");

        boolean result = op.processOrder();

        assertTrue(result, "Credit card order should succeed");
        assertEquals("DONE", op.getStatus());
        assertTrue(op.isDone());
    }

    @Test
    @Order(2)
    void paypalOrder_shouldSucceed() {
        op.setCustomer("Bob Jones", "bob@example.com", "456 Oak Ave, TX", 2);
        op.addItem("USB Hub", 1, 19.99);
        op.setPaypal("bob@example.com");

        boolean result = op.processOrder();

        assertTrue(result, "PayPal order should succeed");
    }

    @Test
    @Order(3)
    void bankTransferOrder_shouldSucceed() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St, NY", 1);
        op.addItem("Keyboard", 1, 79.99);
        op.setBankTransfer("12345678", "123456789");

        boolean result = op.processOrder();

        assertTrue(result, "Bank transfer order should succeed");
    }

    @Test
    @Order(4)
    void emptyOrder_shouldFail() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.setCard("4111111111111111", "12/26", "123");
        // No items added

        boolean result = op.processOrder();

        assertFalse(result, "Empty order should fail");
    }

    @Test
    @Order(5)
    void noCustomer_shouldFail() {
        op.addItem("Mouse", 1, 29.99);
        op.setCard("4111111111111111", "12/26", "123");
        // No customer set

        boolean result = op.processOrder();

        assertFalse(result, "Order without customer should fail");
    }

    @Test
    @Order(6)
    void invalidCard_shouldFail() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.addItem("Mouse", 1, 29.99);
        op.setCard("123", "12/26", "12"); // too short

        boolean result = op.processOrder();

        assertFalse(result, "Invalid card should fail");
    }

    @Test
    @Order(7)
    void declinedCard_shouldFail() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.addItem("Mouse", 1, 29.99);
        op.setCard("1234567890000000", "12/26", "123"); // ends 0000 = declined

        boolean result = op.processOrder();

        assertFalse(result, "Declined card should return false");
    }

    @Test
    @Order(8)
    void doubleSubmit_shouldFail() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.addItem("Mouse", 1, 29.99);
        op.setCard("4111111111111111", "12/26", "123");

        op.processOrder(); // first call
        boolean duplicate = op.processOrder(); // second call

        assertFalse(duplicate, "Double submit should be rejected");
    }

    @Test
    @Order(9)
    void outOfStockProduct_shouldNotBeAdded() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.addItem("Monitor", 100, 399.99); // only 5 in stock
        // Monitor won't be added due to stock check

        assertEquals(0.0, op.getT(), 0.001, "Total should be 0 when item can't be added");
    }

    @Test
    @Order(10)
    void nonExistentProduct_shouldNotBeAdded() {
        op.setCustomer("Alice Smith", "alice@example.com", "123 Main St", 1);
        op.addItem("Hoverboard", 1, 299.99); // doesn't exist

        assertEquals(0.0, op.getT(), 0.001, "Total should be 0 for unknown product");
    }

    @Test
    @Order(11)
    void totalCalculation_shouldBeCorrect() {
        op.setCustomer("Bob Jones", "bob@example.com", "456 Oak Ave", 2);
        op.addItem("Mouse",    1, 29.99);
        op.addItem("Keyboard", 1, 79.99);

        assertEquals(109.98, op.getT(), 0.001, "Total should be sum of items");
    }

    @Test
    @Order(12)
    void initialStatus_shouldBePending() {
        assertEquals("PENDING", op.getStatus());
        assertFalse(op.isDone());
    }
}
