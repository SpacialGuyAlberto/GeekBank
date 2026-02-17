package com.geekbank.bank.order.dto;

import lombok.Data;

@Data
public class OrderResponse {
    private String orderId;
    private double totalPrice;

    public String getOrderId() {
        return orderId;
    }

}
