package com.training.clean.repository;

import com.training.clean.model.Order;
import com.training.clean.service.OrderPricing;

import java.util.Optional;
import java.util.OptionalInt;

public interface OrderRepository {

    Optional<CustomerSnapshot> findCustomer(int customerId);

    OptionalInt findStock(String productName);

    int savePaidOrder(Order order,
                      OrderPricing pricing,
                      String transactionId,
                      int earnedLoyaltyPoints,
                      int redeemedLoyaltyPoints);
}

