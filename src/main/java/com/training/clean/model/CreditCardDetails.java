package com.training.clean.model;

public class CreditCardDetails implements PaymentDetails {

    private final String cardNumber;
    private final String expiry;
    private final String cvv;

    public CreditCardDetails(String cardNumber, String expiry, String cvv) {
        this.cardNumber = cardNumber;
        this.expiry = expiry;
        this.cvv = cvv;
    }

    public String cardNumber() {
        return cardNumber;
    }

    public String expiry() {
        return expiry;
    }

    public String cvv() {
        return cvv;
    }
}

