package com.training.clean.dto;

public class CreateOrderResponseDto {

    private final boolean success;
    private final String status;
    private final int orderId;
    private final double finalTotal;
    private final String transactionId;

    public CreateOrderResponseDto(boolean success, String status, int orderId, double finalTotal, String transactionId) {
        this.success = success;
        this.status = status;
        this.orderId = orderId;
        this.finalTotal = finalTotal;
        this.transactionId = transactionId;
    }

    public boolean success() {
        return success;
    }

    public String status() {
        return status;
    }

    public int orderId() {
        return orderId;
    }

    public double finalTotal() {
        return finalTotal;
    }

    public String transactionId() {
        return transactionId;
    }
}

