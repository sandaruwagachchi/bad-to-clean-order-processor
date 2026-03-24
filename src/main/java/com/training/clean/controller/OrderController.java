package com.training.clean.controller;

import com.training.clean.dto.CreateOrderRequestDto;
import com.training.clean.dto.CreateOrderResponseDto;
import com.training.clean.dto.OrderItemRequestDto;
import com.training.clean.model.Order;
import com.training.clean.model.PaymentMethod;
import com.training.clean.service.OrderProcessingResult;
import com.training.clean.service.OrderService;

import java.util.List;

public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public CreateOrderResponseDto createOrder(CreateOrderRequestDto request) {
        Order order;
        try {
            order = mapToOrder(request);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return new CreateOrderResponseDto(false, "INVALID_REQUEST: " + ex.getMessage(), -1, 0.0, "");
        }

        OrderProcessingResult result = orderService.process(order);
        return new CreateOrderResponseDto(
                result.success(),
                result.status(),
                result.orderId(),
                result.finalTotal(),
                result.transactionId()
        );
    }

    private Order mapToOrder(CreateOrderRequestDto request) {
        Order.Builder builder = Order.builder()
                .customer(request.customerId(), request.customerName(), request.customerEmail(), request.customerAddress());

        List<OrderItemRequestDto> items = request.items();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }

        for (OrderItemRequestDto item : items) {
            builder.addItem(item.productName(), item.quantity(), item.unitPrice());
        }

        PaymentMethod method = request.paymentMethod();
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        return switch (method) {
            case CREDIT_CARD -> builder.creditCard(request.cardNumber(), request.cardExpiry(), request.cardCvv()).build();
            case PAYPAL -> builder.paypal(request.paypalEmail()).build();
            case BANK_TRANSFER -> builder.bankTransfer(request.bankAccount(), request.bankRouting()).build();
        };
    }
}

