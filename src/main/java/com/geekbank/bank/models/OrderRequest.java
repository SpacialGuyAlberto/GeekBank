package com.geekbank.bank.models;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRequest {

    public String getOrderRequestId() {
        return orderRequestId;
    }

    public void setOrderRequestId() {
        this.orderRequestId = "ORQ-" + System.currentTimeMillis();
    }

    private String orderRequestId;
    private Long userId;

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    private String guestId;
    private Long gameUserId;

    private Boolean manual;
    private String phoneNumber;
    private List<Product> products;
    private Double amount;
    private String refNumber;

    private String email;

    private Boolean sendKeyToSMS;

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setOrderRequestId(String orderRequestId) {
        this.orderRequestId = orderRequestId;
    }

    private LocalDateTime createdAt;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount(){
        return this.amount;
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public String getGuestId() {
        return this.guestId;
    }

    public Long getGameUserId() {
        return gameUserId;
    }

    public void setGameUserId(Long gameUserId) {
        this.gameUserId = gameUserId;
    }
    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }
    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Boolean getSendKeyToSMS() {
        return sendKeyToSMS;
    }

    public void setSendKeyToSMS(Boolean sendKeyToSMS) {
        this.sendKeyToSMS = sendKeyToSMS;
    }

    
    public OrderRequest() {
        this.createdAt = LocalDateTime.now();
    }


    public static class Product {
        private int kinguinId;
        private int qty;
        private double price;

        public int getKinguinId() {
            return kinguinId;
        }

        public void setKinguinId(int kinguinId) {
            this.kinguinId = kinguinId;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }


    }
}
