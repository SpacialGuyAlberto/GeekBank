package com.geekbank.bank.support.sms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.geekbank.bank.transaction.model.Transaction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
@Getter
@Setter
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageFrom;
    private double amountReceived;
    private String senderPhoneNumber;
    private String referenceNumber;
    private String date;
    private String time;
    private double newBalance;
    private LocalDateTime receivedAt;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    @JsonBackReference
    private Transaction transaction;

    public SmsMessage() {
    }

    public SmsMessage(String messageFrom, double amountReceived, String senderPhoneNumber,
                      String referenceNumber, String date, String time, double newBalance,
                      LocalDateTime receivedAt) {
        this.messageFrom = messageFrom;
        this.amountReceived = amountReceived;
        this.senderPhoneNumber = senderPhoneNumber;
        this.referenceNumber = referenceNumber;
        this.date = date;
        this.time = time;
        this.newBalance = newBalance;
        this.receivedAt = receivedAt;
    }
}

