package com.geekbank.bank.order.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class OrderRequest {

    private String orderRequestId;

    private Long userId;

    private String guestId;

    private Long gameUserId;

    private Boolean manual;

    private String phoneNumber;

    private List<Product> products;

    private Double amount;

    private String refNumber;

    private String email;

    private Boolean sendKeyToSMS;

    private String referenceCode;

    private String affiliateLink;

    private String promoCode;

    private LocalDateTime createdAt;

    public void setOrderRequestId() {
        this.orderRequestId = "ORQ-" + System.currentTimeMillis();
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
