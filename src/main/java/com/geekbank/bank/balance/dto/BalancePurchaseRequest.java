// src/main/java/com/geekbank/bank/models/BalancePurchaseRequest.java
package com.geekbank.bank.balance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class BalancePurchaseRequest {

    private String id;
    private Long userId;
    private String guestId;
    private String phoneNumber;
    private Double amount;
    private LocalDateTime createdAt;


    public BalancePurchaseRequest() {
        setId();
        this.createdAt = LocalDateTime.now();
    }

    public void setId() {
        this.id = "BAL-" + System.currentTimeMillis();
    }

}
