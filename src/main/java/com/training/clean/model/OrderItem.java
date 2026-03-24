package com.training.clean.model;

public class OrderItem {

    private final String productName;
    private final int quantity;
    private final double unitPrice;

    public OrderItem(String productName, int quantity, double unitPrice) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String productName() {
        return productName;
    }

    public int quantity() {
        return quantity;
    }

    public double unitPrice() {
        return unitPrice;
    }

    public double subtotal() {
        return quantity * unitPrice;
    }
}

