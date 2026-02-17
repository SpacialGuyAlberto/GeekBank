package com.geekbank.bank.payment.tigo.dto;

import com.geekbank.bank.payment.tigo.model.UnmatchedPayment;
import lombok.Data;

import java.util.List;

@Data
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
}
