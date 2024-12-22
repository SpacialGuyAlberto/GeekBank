package com.geekbank.bank.models;

import java.util.List;

public class Receipt {
    private String transactionId;
    private String customerName;
    private String customerEmail;
    private double amountUsd;
    private String date;
    private List<OrderRequest.Product> products; // Nueva lista de productos

    // Constructor
    public Receipt(String transactionId, String customerName, String customerEmail, double amountUsd, String date, List<OrderRequest.Product> products) {
        this.transactionId = transactionId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.amountUsd = amountUsd;
        this.date = date;
        this.products = products;
    }

    // Getters y Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public double getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(double amountUsd) {
        this.amountUsd = amountUsd;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<OrderRequest.Product> getProducts() {
        return products;
    }

    public void setProducts(List<OrderRequest.Product> products) {
        this.products = products;
    }
}
