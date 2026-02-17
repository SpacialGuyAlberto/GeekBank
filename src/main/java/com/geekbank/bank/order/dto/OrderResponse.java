package com.geekbank.bank.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponse {
    private String orderId;
    private double totalPrice;

    public String getOrderId() {
        return orderId;
    }

}
