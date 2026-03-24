package com.training.clean.model;

public class PayPalDetails implements PaymentDetails {

    private final String email;

    public PayPalDetails(String email) {
        this.email = email;
    }

    public String email() {
        return email;
    }
}

