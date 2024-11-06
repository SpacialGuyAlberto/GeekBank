package com.geekbank.bank.dto;

import java.util.List;

public class TransactionRequest {
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    private Long userId; // Opcional, para usuarios autenticados
    private String guestId; // Opcional, para usuarios guest
    private String phoneNumber;
    private List<Product> products;
    private Double amount;

    // Getters y setters

    public static class Product {
        private Long kinguinId;
        private Integer qty;
        private Double price;
        private String name; // Opcional, para balance

        // Getters y setters
    }
}
