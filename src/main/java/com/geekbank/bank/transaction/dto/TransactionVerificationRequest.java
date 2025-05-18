package com.geekbank.bank.transaction.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionVerificationRequest {
    private Long tempPin;
    private String refNumber;
    private String phoneNumber;
}
