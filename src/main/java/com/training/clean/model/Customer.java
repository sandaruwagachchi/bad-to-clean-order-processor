package com.training.clean.model;

public class Customer {

    private final int id;
    private final String name;
    private final String email;
    private final String address;

    public Customer(int id, String name, String email, String address) {
        if (id <= 0) {
            throw new IllegalArgumentException("Customer id must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid customer email is required");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Customer address is required");
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
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

    public String address() {
        return address;
    }
}

