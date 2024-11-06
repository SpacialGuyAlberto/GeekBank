package com.geekbank.bank.models;

public class TransactionResponse {
    private String orderRequestNumber;
    private String transactionNumber;
    private Long tempPin;

    // Getters y setters

    public String getOrderRequestNumber() {
        return orderRequestNumber;
    }

    public void setOrderRequestNumber(String orderRequestNumber) {
        this.orderRequestNumber = orderRequestNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
    public Long getTempPin() {
        return tempPin;
    }

    public void setTempPin(Long tempPin) {
        this.tempPin = tempPin;
    }
}
