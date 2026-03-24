package com.training.clean.service;

import com.training.bad.FakePaymentGateway;
import com.training.clean.model.Order;
import com.training.clean.model.OrderItem;
import com.training.clean.notification.NotificationService;
import com.training.clean.payment.PaymentProcessor;
import com.training.clean.payment.PaymentResult;
import com.training.clean.repository.CustomerSnapshot;
import com.training.clean.repository.OrderRepository;

import java.util.Optional;
import java.util.OptionalInt;

public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentProcessor paymentProcessor;
    private final DiscountCalculator discountCalculator;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        PaymentProcessor paymentProcessor,
                        DiscountCalculator discountCalculator,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.paymentProcessor = paymentProcessor;
        this.discountCalculator = discountCalculator;
        this.notificationService = notificationService;
    }

    public OrderProcessingResult process(Order order) {
        Optional<CustomerSnapshot> customerOpt = orderRepository.findCustomer(order.customer().id());
        if (customerOpt.isEmpty()) {
            return OrderProcessingResult.failed("CUSTOMER_NOT_FOUND");
        }

        for (OrderItem item : order.items()) {
            OptionalInt stockOpt = orderRepository.findStock(item.productName());
            if (stockOpt.isEmpty()) {
                return OrderProcessingResult.failed("PRODUCT_NOT_FOUND: " + item.productName());
            }
            if (stockOpt.getAsInt() < item.quantity()) {
                return OrderProcessingResult.failed("OUT_OF_STOCK: " + item.productName());
            }
        }

        CustomerSnapshot customer = customerOpt.get();
        OrderPricing pricing = discountCalculator.calculate(order.subtotal(), customer.loyaltyPoints());

        PaymentResult payment = paymentProcessor.processPayment(order, pricing.finalTotal());
        if (!payment.success()) {
            return OrderProcessingResult.failed("PAYMENT_FAILED: " + payment.message());
        }

        int earnedPoints = (int) (pricing.finalTotal() / 10);
        int redeemedPoints = (int) (pricing.loyaltyDiscount() / 0.01);

        int orderId;
        try {
            orderId = orderRepository.savePaidOrder(order, pricing, payment.transactionId(), earnedPoints, redeemedPoints);
        } catch (RuntimeException ex) {
            return OrderProcessingResult.failed("SAVE_FAILED");
        }

        notificationService.sendOrderConfirmation(orderId, order, pricing, payment.transactionId());
        FakePaymentGateway.sendAnalyticsEvent(orderId, pricing.finalTotal(), order.customer().id());

        return OrderProcessingResult.success(orderId, pricing.finalTotal(), payment.transactionId());
    }
}

