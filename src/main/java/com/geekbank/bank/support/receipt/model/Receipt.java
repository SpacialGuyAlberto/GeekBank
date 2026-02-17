package com.geekbank.bank.support.receipt.model;

import com.geekbank.bank.order.dto.OrderRequest;
import lombok.Data;

import java.util.List;

@Data
public class Receipt {
    private String transactionId;
    private String customerName;
    private String customerEmail;
    private double amountUsd;
    private String date;
    private List<OrderRequest.Product> products;


    public Receipt(String transactionId, String customerName, String customerEmail, double amountUsd, String date, List<OrderRequest.Product> products) {
        this.transactionId = transactionId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.amountUsd = amountUsd;
        this.date = date;
        this.products = products;
    }
}
