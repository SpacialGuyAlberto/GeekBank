// src/main/java/com/geekbank/bank/models/BalancePurchaseRequest.java
package com.geekbank.bank.models;

import java.time.LocalDateTime;

public class BalancePurchaseRequest {

    private String id;
    private Long userId;
    private String guestId;
    private String phoneNumber;
    private Double amount;
    private LocalDateTime createdAt;


    public BalancePurchaseRequest() {
        setId();
        this.createdAt = LocalDateTime.now();
    }


    public String getId() {
        return id;
    }

    public void setId() {
        this.id = "BAL-" + System.currentTimeMillis();
    }

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

    public Double getAmount(){
        return this.amount;
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
