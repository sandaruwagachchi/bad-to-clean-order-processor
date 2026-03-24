package com.training.clean.payment;

import com.training.clean.model.Order;
import com.training.clean.model.PaymentMethod;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PaymentProcessor {

    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    public PaymentProcessor(List<PaymentStrategy> strategies) {
        this.strategiesByMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategies) {
            this.strategiesByMethod.put(strategy.supportedMethod(), strategy);
        }
    }

    public PaymentResult processPayment(Order order, double amount) {
        PaymentStrategy strategy = strategiesByMethod.get(order.paymentMethod());
        if (strategy == null) {
            return PaymentResult.declined("Unsupported payment method: " + order.paymentMethod());
        }
        return strategy.pay(order.paymentDetails(), amount);
    }
}

