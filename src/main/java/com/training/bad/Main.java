package com.training.bad;

import com.training.clean.config.CleanApplicationFactory;
import com.training.clean.controller.OrderController;
import com.training.clean.dto.CreateOrderRequestDto;
import com.training.clean.dto.CreateOrderResponseDto;
import com.training.clean.dto.OrderItemRequestDto;
import com.training.clean.model.PaymentMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Entry point for the refactored clean architecture flow.
 * Run with: mvn exec:java
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        OrderController controller = CleanApplicationFactory.createOrderController();

        log.info("==============================================");
        log.info(" CLEAN Order Processing - Day 4 Demo ");
        log.info("==============================================");

        CreateOrderResponseDto result1 = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St, Springfield, NY",
                List.of(
                        new OrderItemRequestDto("Mouse", 2, 29.99),
                        new OrderItemRequestDto("Keyboard", 1, 79.99)
                ),
                PaymentMethod.CREDIT_CARD,
                "4111111111111111",
                "12/26",
                "123",
                null,
                null,
                null
        ));
        logResult("Scenario 1: Credit Card", result1);

        CreateOrderResponseDto result2 = controller.createOrder(new CreateOrderRequestDto(
                2,
                "Bob Jones",
                "bob@example.com",
                "456 Oak Ave, Houston, TX",
                List.of(new OrderItemRequestDto("USB Hub", 2, 19.99)),
                PaymentMethod.PAYPAL,
                null,
                null,
                null,
                "bob@example.com",
                null,
                null
        ));
        logResult("Scenario 2: PayPal", result2);

        CreateOrderResponseDto result3 = controller.createOrder(new CreateOrderRequestDto(
                1,
                "Alice Smith",
                "alice@example.com",
                "123 Main St, Springfield, NY",
                List.of(
                        new OrderItemRequestDto("Laptop", 1, 999.99),
                        new OrderItemRequestDto("Monitor", 1, 399.99)
                ),
                PaymentMethod.BANK_TRANSFER,
                null,
                null,
                null,
                null,
                "12345678",
                "123456789"
        ));
        logResult("Scenario 3: Bank Transfer", result3);

        CreateOrderResponseDto result4 = controller.createOrder(new CreateOrderRequestDto(
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
        logResult("Scenario 4: Unknown Product", result4);

        CreateOrderResponseDto result5 = controller.createOrder(new CreateOrderRequestDto(
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
        logResult("Scenario 5: Declined Card", result5);

        log.info("==============================================");
        log.info("Check invoice_*.txt files and orders.log in project root");
        log.info("==============================================");
    }

    private static void logResult(String scenario, CreateOrderResponseDto response) {
        log.info("{} => {} | status={} | orderId={} | total={} | tx={}",
                scenario,
                response.success() ? "SUCCESS" : "FAILED",
                response.status(),
                response.orderId(),
                String.format("%.2f", response.finalTotal()),
                response.transactionId());
    }
}
