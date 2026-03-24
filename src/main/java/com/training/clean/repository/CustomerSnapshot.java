package com.training.clean.repository;

public class CustomerSnapshot {

    private final int id;
    private final String name;
    private final String email;
    private final int loyaltyPoints;

    public CustomerSnapshot(int id, String name, String email, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.loyaltyPoints = loyaltyPoints;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public int loyaltyPoints() {
        return loyaltyPoints;
    }
}

