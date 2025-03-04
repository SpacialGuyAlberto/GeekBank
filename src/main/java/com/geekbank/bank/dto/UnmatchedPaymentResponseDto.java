package com.geekbank.bank.dto;

import com.geekbank.bank.models.UnmatchedPayment;

import java.util.List;

public class UnmatchedPaymentResponseDto {

    private UnmatchedPayment unmatchedPayment;
    private double receivedAmount;
    private double expectedAmount;
    private double difference;
    private String message;
    private List<String> options;

    public UnmatchedPaymentResponseDto(UnmatchedPayment unmatchedPayment, double receivedAmount, double expectedAmount, double difference, String message, List<String> options) {
        this.unmatchedPayment = unmatchedPayment;
        this.receivedAmount = receivedAmount;
        this.expectedAmount = expectedAmount;
        this.difference = difference;
        this.message = message;
        this.options = options;
    }

    public UnmatchedPaymentResponseDto() {

    }

    public UnmatchedPayment getUnmatchedPayment() {
        return unmatchedPayment;
    }

    public void setUnmatchedPayment(UnmatchedPayment unmatchedPayment) {
        this.unmatchedPayment = unmatchedPayment;
    }

    public double getReceivedAmount() {
        return receivedAmount;
    }

    public void setReceivedAmount(double receivedAmount) {
        this.receivedAmount = receivedAmount;
    }

    public double getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(double expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
