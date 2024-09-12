package com.geekbank.bank.models;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<KinguinGiftCard> getProducts() {
        return products.stream().map(product -> {
            KinguinGiftCard giftCard = new KinguinGiftCard();
            giftCard.setKinguinId(product.getKinguinId());
            giftCard.setQty(product.getQty());
            giftCard.setPrice(product.getPrice());
            return giftCard;
        }).collect(Collectors.toList());
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
