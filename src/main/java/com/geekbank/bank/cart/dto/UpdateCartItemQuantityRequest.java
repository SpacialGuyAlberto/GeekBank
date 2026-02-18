package com.geekbank.bank.cart.dto;

import lombok.Data;

@Data
public class UpdateCartItemQuantityRequest {
    private Long productId;
    private int quantity;
}
