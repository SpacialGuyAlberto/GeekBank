package com.geekbank.bank.models;
import java.util.List;

public class OrderRequest {

    private Long userId;
    private String phoneNumber;
    private List<Product> products;
    private Double amount;

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
