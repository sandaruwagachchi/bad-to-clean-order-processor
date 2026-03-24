package com.training.bad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub that replaces real HTTP calls to payment gateways and analytics.
 *
 * NOTE FOR TRAINING: This is still a bad design - the OrderProcessor is
 * tightly coupled to this concrete class. In the refactored version this
 * should be behind a PaymentStrategy interface.
 */
public class FakePaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(FakePaymentGateway.class);

    public static class ChargeResult {
        private final boolean success;
        private final String message;

        public ChargeResult(boolean success, String message) {
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

    public static ChargeResult chargeCreditCard(String apiKey, String cardNum,
                                                 String exp, String cvv, double amount) {
        log.info("[STUB] POST https://payment-gateway.com/api/charge");
        log.info("[STUB] Charging card **** **** **** {} | amount: ${}", 
                 cardNum.substring(12), String.format("%.2f", amount));
        // Simulate: cards ending in 0000 are declined
        if (cardNum.endsWith("0000")) {
            return new ChargeResult(false, "Card declined");
        }
        return new ChargeResult(true, "Approved");
    }

    public static ChargeResult chargePaypal(String apiKey, String paypalEmail, double amount) {
        log.info("[STUB] POST https://api.paypal.com/v1/payments/payment");
        log.info("[STUB] Charging PayPal account {} | amount: ${}", 
                 paypalEmail, String.format("%.2f", amount));
        // Simulate: test@fail.com is always declined
        if ("test@fail.com".equals(paypalEmail)) {
            return new ChargeResult(false, "PayPal account restricted");
        }
        return new ChargeResult(true, "Approved");
    }

    public static ChargeResult chargeBankTransfer(String apiKey, String account,
                                                   String routing, double amount) {
        log.info("[STUB] POST https://banking-api.com/transfer");
        log.info("[STUB] ACH transfer from account ...{} routing {} | amount: ${}",
                 account.substring(account.length() - 4), routing, String.format("%.2f", amount));
        return new ChargeResult(true, "Transfer initiated");
    }

    public static void sendAnalyticsEvent(int orderId, double value, int customerId) {
        log.info("[STUB] POST https://analytics.company.com/api/event");
        log.info("[STUB] event=order_placed order_id={} value={} customer_id={}",
                 orderId, String.format("%.2f", value), customerId);
    }
}
