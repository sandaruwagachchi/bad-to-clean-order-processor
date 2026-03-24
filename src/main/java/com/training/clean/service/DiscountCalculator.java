package com.training.clean.service;

public class DiscountCalculator {

    private static final double TAX_RATE = 0.08;
    private static final double TIER_DISCOUNT_1 = 0.05;
    private static final double TIER_DISCOUNT_2 = 0.10;
    private static final double TIER_DISCOUNT_3 = 0.20;

    private static final int TIER_THRESHOLD_1 = 100;
    private static final int TIER_THRESHOLD_2 = 500;
    private static final int TIER_THRESHOLD_3 = 1000;

    private static final int LOYALTY_MIN_POINTS = 100;
    private static final double LOYALTY_POINT_VALUE = 0.01;
    private static final double LOYALTY_MAX_RATE = 0.15;

    public OrderPricing calculate(double subtotal, int loyaltyPoints) {
        double tierRate = resolveTierRate(subtotal);
        double tierDiscount = subtotal * tierRate;
        double afterTier = subtotal - tierDiscount;

        double loyaltyDiscount = 0.0;
        if (loyaltyPoints >= LOYALTY_MIN_POINTS) {
            loyaltyDiscount = Math.min(loyaltyPoints * LOYALTY_POINT_VALUE, afterTier * LOYALTY_MAX_RATE);
        }

        double taxableAmount = afterTier - loyaltyDiscount;
        double taxAmount = taxableAmount * TAX_RATE;
        double finalTotal = taxableAmount + taxAmount;

        return new OrderPricing(subtotal, tierDiscount, loyaltyDiscount, taxAmount, finalTotal);
    }

    private double resolveTierRate(double subtotal) {
        if (subtotal >= TIER_THRESHOLD_3) {
            return TIER_DISCOUNT_3;
        }
        if (subtotal >= TIER_THRESHOLD_2) {
            return TIER_DISCOUNT_2;
        }
        if (subtotal >= TIER_THRESHOLD_1) {
            return TIER_DISCOUNT_1;
        }
        return 0.0;
    }
}

