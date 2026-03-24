package com.training.clean.payment;

import com.training.clean.model.CreditCardDetails;
import com.training.clean.model.PaymentDetails;
import com.training.clean.model.PaymentMethod;

public class CreditCardPaymentStrategy implements PaymentStrategy {

    private final PaymentGateway paymentGateway;

    public CreditCardPaymentStrategy(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult pay(PaymentDetails paymentDetails, double amount) {
        if (!(paymentDetails instanceof CreditCardDetails details)) {
            return PaymentResult.declined("Invalid credit card details");
        }
        if (details.cardNumber() == null || details.cardNumber().length() != 16) {
            return PaymentResult.declined("Invalid card number");
        }
        if (details.cvv() == null || details.cvv().length() < 3) {
            return PaymentResult.declined("Invalid CVV");
        }

        PaymentGateway.GatewayResult result =
                paymentGateway.chargeCreditCard(details.cardNumber(), details.expiry(), details.cvv(), amount);

        if (!result.success()) {
            return PaymentResult.declined(result.message());
        }
        return PaymentResult.approved("CC-" + System.currentTimeMillis());
    }
}

