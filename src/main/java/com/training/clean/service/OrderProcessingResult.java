package com.training.clean.service;

public class OrderProcessingResult {

    private final boolean success;
    private final String status;
    private final int orderId;
    private final double finalTotal;
    private final String transactionId;

    public OrderProcessingResult(boolean success, String status, int orderId, double finalTotal, String transactionId) {
        this.success = success;
        this.status = status;
        this.orderId = orderId;
        this.finalTotal = finalTotal;
        this.transactionId = transactionId;
    }

    public static OrderProcessingResult failed(String status) {
        return new OrderProcessingResult(false, status, -1, 0.0, "");
    }

    public static OrderProcessingResult success(int orderId, double finalTotal, String transactionId) {
        return new OrderProcessingResult(true, "DONE", orderId, finalTotal, transactionId);
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

