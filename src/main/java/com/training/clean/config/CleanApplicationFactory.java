package com.training.clean.config;

import com.training.clean.controller.OrderController;
import com.training.clean.notification.FileNotificationService;
import com.training.clean.payment.*;
import com.training.clean.repository.H2OrderRepository;
import com.training.clean.repository.OrderRepository;
import com.training.clean.service.DiscountCalculator;
import com.training.clean.service.OrderService;

import java.util.List;

public final class CleanApplicationFactory {

    private CleanApplicationFactory() {
    }

    public static OrderController createOrderController() {
        OrderRepository repository = new H2OrderRepository();
        PaymentGateway gateway = new FakePaymentGatewayAdapter("sk-abc123secret");

        PaymentProcessor paymentProcessor = new PaymentProcessor(List.of(
                new CreditCardPaymentStrategy(gateway),
                new PayPalPaymentStrategy(gateway),
                new BankTransferPaymentStrategy(gateway)
        ));

        OrderService orderService = new OrderService(
                repository,
                paymentProcessor,
                new DiscountCalculator(),
                new FileNotificationService()
        );

        return new OrderController(orderService);
    }
}

