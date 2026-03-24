package com.training.clean.dto;

import com.training.clean.model.PaymentMethod;

import java.util.List;

public class CreateOrderRequestDto {

    private final int customerId;
    private final String customerName;
    private final String customerEmail;
    private final String customerAddress;
    private final List<OrderItemRequestDto> items;
    private final PaymentMethod paymentMethod;
    private final String cardNumber;
    private final String cardExpiry;
    private final String cardCvv;
    private final String paypalEmail;
    private final String bankAccount;
    private final String bankRouting;

    public CreateOrderRequestDto(int customerId,
                                 String customerName,
                                 String customerEmail,
                                 String customerAddress,
                                 List<OrderItemRequestDto> items,
                                 PaymentMethod paymentMethod,
                                 String cardNumber,
                                 String cardExpiry,
                                 String cardCvv,
                                 String paypalEmail,
                                 String bankAccount,
                                 String bankRouting) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerAddress = customerAddress;
        this.items = items;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
        this.cardCvv = cardCvv;
        this.paypalEmail = paypalEmail;
        this.bankAccount = bankAccount;
        this.bankRouting = bankRouting;
    }

    public int customerId() {
        return customerId;
    }

    public String customerName() {
        return customerName;
    }

    public String customerEmail() {
        return customerEmail;
    }

    public String customerAddress() {
        return customerAddress;
    }

    public List<OrderItemRequestDto> items() {
        return items;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    public String cardNumber() {
        return cardNumber;
    }

    public String cardExpiry() {
        return cardExpiry;
    }

    public String cardCvv() {
        return cardCvv;
    }

    public String paypalEmail() {
        return paypalEmail;
    }

    public String bankAccount() {
        return bankAccount;
    }

    public String bankRouting() {
        return bankRouting;
    }
}

