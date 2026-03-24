package com.training.clean.payment;

import com.training.clean.model.PayPalDetails;
import com.training.clean.model.PaymentDetails;
import com.training.clean.model.PaymentMethod;

public class PayPalPaymentStrategy implements PaymentStrategy {

    private final PaymentGateway paymentGateway;

    public PayPalPaymentStrategy(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public PaymentResult pay(PaymentDetails paymentDetails, double amount) {
        if (!(paymentDetails instanceof PayPalDetails details)) {
            return PaymentResult.declined("Invalid PayPal details");
        }
        if (details.email() == null || !details.email().contains("@")) {
            return PaymentResult.declined("Invalid PayPal email");
        }

        PaymentGateway.GatewayResult result = paymentGateway.chargePaypal(details.email(), amount);
        if (!result.success()) {
            return PaymentResult.declined(result.message());
        }
        return PaymentResult.approved("PP-" + System.currentTimeMillis());
    }
}

