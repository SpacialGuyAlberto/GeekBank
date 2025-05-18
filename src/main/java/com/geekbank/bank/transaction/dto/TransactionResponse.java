package com.geekbank.bank.transaction.dto;

import com.geekbank.bank.transaction.constants.TransactionStatus;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TransactionResponse {
    private String orderRequestNumber;
    private String transactionNumber;
    private Long tempPin;
    private TransactionStatus transactionStatus;

}
