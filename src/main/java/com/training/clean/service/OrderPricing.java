package com.training.clean.service;

public class OrderPricing {

    private final double subtotal;
    private final double tierDiscount;
    private final double loyaltyDiscount;
    private final double taxAmount;
    private final double finalTotal;

    public OrderPricing(double subtotal,
                        double tierDiscount,
                        double loyaltyDiscount,
                        double taxAmount,
                        double finalTotal) {
        this.subtotal = subtotal;
        this.tierDiscount = tierDiscount;
        this.loyaltyDiscount = loyaltyDiscount;
        this.taxAmount = taxAmount;
        this.finalTotal = finalTotal;
    }

    public double subtotal() {
        return subtotal;
    }

    public double tierDiscount() {
        return tierDiscount;
    }

    public double loyaltyDiscount() {
        return loyaltyDiscount;
    }

    public double taxAmount() {
        return taxAmount;
    }

    public double finalTotal() {
        return finalTotal;
    }

    public double totalDiscount() {
        return tierDiscount + loyaltyDiscount;
    }
}

