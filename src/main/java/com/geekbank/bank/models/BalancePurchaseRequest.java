package com.geekbank.bank.models;

import java.time.LocalDateTime;

public class BalancePurchaseRequest {

    public String getId() {
        return id;
    }

    public void setId() {
        this.id= "BR-" + System.currentTimeMillis();
    }

    private String id;
    private Long userId;
    private String phoneNumber;
    private Double amount;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private LocalDateTime createdAt;

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
       this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
