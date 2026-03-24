package com.training.clean.payment;

public interface PaymentGateway {

    GatewayResult chargeCreditCard(String cardNumber, String expiry, String cvv, double amount);

    GatewayResult chargePaypal(String email, double amount);

    GatewayResult chargeBankTransfer(String accountNumber, String routingNumber, double amount);

    final class GatewayResult {
        private final boolean success;
        private final String message;

        public GatewayResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }
    }
}

