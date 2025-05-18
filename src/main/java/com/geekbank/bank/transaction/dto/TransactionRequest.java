package com.geekbank.bank.transaction.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class TransactionRequest {

    private Long userId;
    private String guestId;
    private String phoneNumber;
    private List<Product> products;
    private Double amount;


    public static class Product {
        private Long kinguinId;
        private Integer qty;
        private Double price;
        private String name;
    }
}
