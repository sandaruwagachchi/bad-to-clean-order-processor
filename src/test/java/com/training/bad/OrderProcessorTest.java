package com.training.bad;

import com.training.clean.controller.OrderController;
import com.training.clean.dto.CreateOrderRequestDto;
import com.training.clean.dto.CreateOrderResponseDto;
import com.training.clean.dto.OrderItemRequestDto;
import com.training.clean.model.Order;
import com.training.clean.model.PaymentMethod;
import com.training.clean.notification.NotificationService;
import com.training.clean.payment.*;
import com.training.clean.repository.H2OrderRepository;
import com.training.clean.repository.OrderRepository;
import com.training.clean.service.DiscountCalculator;
import com.training.clean.service.OrderPricing;
import com.training.clean.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private OrderController controller;

    @BeforeEach
    void setUp() {
        String dbUrl = "jdbc:h2:mem:shop-test-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        OrderRepository repository = new H2OrderRepository(dbUrl, "sa", "");

        PaymentGateway gateway = new FakePaymentGatewayAdapter("sk-test");
        PaymentProcessor processor = new PaymentProcessor(List.of(
                new CreditCardPaymentStrategy(gateway),
                new PayPalPaymentStrategy(gateway),
                new BankTransferPaymentStrategy(gateway)
        ));

        NotificationService notificationService = (orderId, order, pricing, txId) -> {
            // Keep tests focused on order processing outcome.
        };

        OrderService service = new OrderService(repository, processor, new DiscountCalculator(), notificationService);
        controller = new OrderController(service);
    }

    @Test
    void creditCardOrder_shouldSucceed() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(new OrderItemRequestDto("Mouse", 1, 29.99)),
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "12/26",
                "123",
                null,
                null,
                null
        ));

        assertTrue(response.success());
        assertEquals("DONE", response.status());
        assertTrue(response.orderId() > 0);
    }

    @Test
    void paypalOrder_shouldSucceed() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                2,
                "Bob Jones",
                "bob@example.com",
                "456 Oak Ave",
                List.of(new OrderItemRequestDto("USB Hub", 1, 19.99)),
                PaymentMethod.PAYPAL,
                null,
                null,
                null,
                "bob@example.com",
                null,
                null
        ));

        assertTrue(response.success());
    }

    @Test
    void bankTransfer_shouldSucceed() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(new OrderItemRequestDto("Keyboard", 1, 79.99)),
                PaymentMethod.BANK_TRANSFER,
                null,
                null,
                null,
                null,
                "12345678",
                "123456789"
        ));

        assertTrue(response.success());
    }

    @Test
    void declinedCard_shouldFail() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(new OrderItemRequestDto("Mouse", 1, 29.99)),
                PaymentMethod.CREDIT_CARD,
                "1234567890000000",
                "12/26",
                "999",
                null,
                null,
                null
        ));

        assertFalse(response.success());
        assertTrue(response.status().startsWith("PAYMENT_FAILED"));
    }

    @Test
    void outOfStock_shouldFail() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(new OrderItemRequestDto("Monitor", 100, 399.99)),
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "12/26",
                "123",
                null,
                null,
                null
        ));

        assertFalse(response.success());
        assertTrue(response.status().startsWith("OUT_OF_STOCK"));
    }

    @Test
    void missingProduct_shouldFail() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(new OrderItemRequestDto("Hoverboard", 1, 299.99)),
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "12/26",
                "123",
                null,
                null,
                null
        ));

        assertFalse(response.success());
        assertTrue(response.status().startsWith("PRODUCT_NOT_FOUND"));
    }

    @Test
    void invalidRequest_shouldFailInController() {
        CreateOrderResponseDto response = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St",
                List.of(),
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "12/26",
                "123",
                null,
                null,
                null
        ));

        assertFalse(response.success());
        assertTrue(response.status().startsWith("INVALID_REQUEST"));
    }

    @Test
    void orderBuilder_withoutPayment_shouldThrow() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                Order.builder()
                        .customer(1, "Alice Smith", "alice@example.com", "123 Main St")
                        .addItem("Mouse", 1, 29.99)
                        .build()
        );

        assertTrue(exception.getMessage().contains("Payment details"));
    }

    @Test
    void discountCalculator_shouldApplySameRules() {
        DiscountCalculator calculator = new DiscountCalculator();
        OrderPricing pricing = calculator.calculate(109.98, 50);

        assertEquals(109.98, pricing.subtotal(), 0.001);
        assertEquals(5.499, pricing.tierDiscount(), 0.001);
        assertEquals(0.0, pricing.loyaltyDiscount(), 0.001);
        assertEquals(8.35848, pricing.taxAmount(), 0.001);
        assertEquals(112.83948, pricing.finalTotal(), 0.001);
    }
}
