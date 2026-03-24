package com.training.clean.payment;

public class PaymentResult {

    private final boolean success;
    private final String message;
    private final String transactionId;

    public PaymentResult(boolean success, String message, String transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }

    public static PaymentResult approved(String txId) {
        return new PaymentResult(true, "Approved", txId);
    }

    public static PaymentResult declined(String message) {
        return new PaymentResult(false, message, "");
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public String transactionId() {
        return transactionId;
    }
}

