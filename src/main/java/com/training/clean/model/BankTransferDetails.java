package com.training.clean.model;

public class BankTransferDetails implements PaymentDetails {

    private final String accountNumber;
    private final String routingNumber;

    public BankTransferDetails(String accountNumber, String routingNumber) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
    }

    public String accountNumber() {
        return accountNumber;
    }

    public String routingNumber() {
        return routingNumber;
    }
}

