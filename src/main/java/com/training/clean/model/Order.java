package com.training.clean.model;

import java.util.ArrayList;
import java.util.List;

public final class Order {

    private final Customer customer;
    private final List<OrderItem> items;
    private final PaymentMethod paymentMethod;
    private final PaymentDetails paymentDetails;

    private Order(Builder builder) {
        this.customer = builder.customer;
        this.items = List.copyOf(builder.items);
        this.paymentMethod = builder.paymentMethod;
        this.paymentDetails = builder.paymentDetails;
    }

    public Customer customer() {
        return customer;
    }

    public List<OrderItem> items() {
        return items;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    public PaymentDetails paymentDetails() {
        return paymentDetails;
    }

    public double subtotal() {
        return items.stream().mapToDouble(OrderItem::subtotal).sum();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Customer customer;
        private final List<OrderItem> items = new ArrayList<>();
        private PaymentMethod paymentMethod;
        private PaymentDetails paymentDetails;

        public Builder customer(int id, String name, String email, String address) {
            this.customer = new Customer(id, name, email, address);
            return this;
        }

        public Builder addItem(String productName, int quantity, double unitPrice) {
            this.items.add(new OrderItem(productName, quantity, unitPrice));
            return this;
        }

        public Builder creditCard(String cardNumber, String expiry, String cvv) {
            this.paymentMethod = PaymentMethod.CREDIT_CARD;
            this.paymentDetails = new CreditCardDetails(cardNumber, expiry, cvv);
            return this;
        }

        public Builder paypal(String email) {
            this.paymentMethod = PaymentMethod.PAYPAL;
            this.paymentDetails = new PayPalDetails(email);
            return this;
        }

        public Builder bankTransfer(String accountNumber, String routingNumber) {
            this.paymentMethod = PaymentMethod.BANK_TRANSFER;
            this.paymentDetails = new BankTransferDetails(accountNumber, routingNumber);
            return this;
        }

        public Order build() {
            if (customer == null) {
                throw new IllegalStateException("Customer must be provided");
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("At least one item is required");
            }
            if (paymentMethod == null || paymentDetails == null) {
                throw new IllegalStateException("Payment details must be provided");
            }
            return new Order(this);
        }
    }
}

