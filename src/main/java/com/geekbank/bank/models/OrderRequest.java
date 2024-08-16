package com.geekbank.bank.models;

import java.util.List;
import java.util.stream.Collectors;

public class OrderRequest {
    private String phoneNumber;
    private List<Product> products;

    // Getter for phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Setter for phoneNumber
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getter for products
    // Option 1: Convert Product to KinguinGiftCard
    public List<KinguinGiftCard> getProducts() {
        return products.stream().map(product -> {
            KinguinGiftCard giftCard = new KinguinGiftCard();
            giftCard.setKinguinId(product.getKinguinId());
            giftCard.setQty(product.getQty());
            giftCard.setPrice(product.getPrice());
            return giftCard;
        }).collect(Collectors.toList());
    }

    // Setter for products
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public static class Product {
        private int kinguinId;
        private int qty;
        private double price;

        // Getters and setters for Product
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
