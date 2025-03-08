package com.geekbank.bank.models;

import java.util.List;

public class OrderDetails {

    private Long userId;
    private String guestId;
    private String phoneNumber;
    private List<Product> products;
    private double amount;
    private boolean manual;
    private Long gameUserId;


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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public Long getGameUserId() {
        return gameUserId;
    }

    public void setGameUserId(Long gameUserId) {
        this.gameUserId = gameUserId;
    }

    public static class Product {
        private int kinguinId;
        private int qty;
        private double price;
        private String name;


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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}