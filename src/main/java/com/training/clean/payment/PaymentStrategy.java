package com.training.clean.payment;

import com.training.clean.model.PaymentDetails;
import com.training.clean.model.PaymentMethod;

public interface PaymentStrategy {

    PaymentMethod supportedMethod();

    PaymentResult pay(PaymentDetails paymentDetails, double amount);
}

