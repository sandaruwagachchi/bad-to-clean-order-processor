package com.training.clean.notification;

import com.training.clean.model.Order;
import com.training.clean.service.OrderPricing;

public interface NotificationService {

    void sendOrderConfirmation(int orderId, Order order, OrderPricing pricing, String transactionId);
}

