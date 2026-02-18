package com.geekbank.bank.cart.dto;

import lombok.Data;

@Data
public class AddCartItemRequest {
    private Long productId;
    private int quantity;
    private double price;

}
