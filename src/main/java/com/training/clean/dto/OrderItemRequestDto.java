package com.training.clean.dto;

public class OrderItemRequestDto {

    private final String productName;
    private final int quantity;
    private final double unitPrice;

    public OrderItemRequestDto(String productName, int quantity, double unitPrice) {
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
}

