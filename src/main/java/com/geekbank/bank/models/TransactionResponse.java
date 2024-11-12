package com.geekbank.bank.models;

public class TransactionResponse {
    private String orderRequestNumber;
    private String transactionNumber;
    private Long tempPin;

    private TransactionStatus transactionStatus;

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
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

}
