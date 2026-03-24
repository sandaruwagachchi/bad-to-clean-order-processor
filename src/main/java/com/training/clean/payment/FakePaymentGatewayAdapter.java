package com.training.clean.payment;

import com.training.bad.FakePaymentGateway;

public class FakePaymentGatewayAdapter implements PaymentGateway {

    private final String apiKey;

    public FakePaymentGatewayAdapter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public GatewayResult chargeCreditCard(String cardNumber, String expiry, String cvv, double amount) {
        FakePaymentGateway.ChargeResult result =
                FakePaymentGateway.chargeCreditCard(apiKey, cardNumber, expiry, cvv, amount);
        return new GatewayResult(result.success(), result.message());
    }

    @Override
    public GatewayResult chargePaypal(String email, double amount) {
        FakePaymentGateway.ChargeResult result = FakePaymentGateway.chargePaypal(apiKey, email, amount);
        return new GatewayResult(result.success(), result.message());
    }

    @Override
    public GatewayResult chargeBankTransfer(String accountNumber, String routingNumber, double amount) {
        FakePaymentGateway.ChargeResult result =
                FakePaymentGateway.chargeBankTransfer(apiKey, accountNumber, routingNumber, amount);
        return new GatewayResult(result.success(), result.message());
    }
}

