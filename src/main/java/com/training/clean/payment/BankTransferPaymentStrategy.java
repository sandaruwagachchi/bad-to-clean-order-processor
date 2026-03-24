package com.training.clean.payment;

import com.training.clean.model.BankTransferDetails;
import com.training.clean.model.PaymentDetails;
import com.training.clean.model.PaymentMethod;

public class BankTransferPaymentStrategy implements PaymentStrategy {

    private final PaymentGateway paymentGateway;

    public BankTransferPaymentStrategy(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public PaymentResult pay(PaymentDetails paymentDetails, double amount) {
        if (!(paymentDetails instanceof BankTransferDetails details)) {
            return PaymentResult.declined("Invalid bank transfer details");
        }
        if (details.accountNumber() == null || details.accountNumber().length() < 8) {
            return PaymentResult.declined("Invalid bank account number");
        }
        if (details.routingNumber() == null || details.routingNumber().length() != 9) {
            return PaymentResult.declined("Invalid routing number");
        }

        PaymentGateway.GatewayResult result =
                paymentGateway.chargeBankTransfer(details.accountNumber(), details.routingNumber(), amount);

        if (!result.success()) {
            return PaymentResult.declined(result.message());
        }
        return PaymentResult.approved("BT-" + System.currentTimeMillis());
    }
}

