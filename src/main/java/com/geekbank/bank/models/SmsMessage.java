package com.geekbank.bank.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public double getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(double amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        this.senderPhoneNumber = senderPhoneNumber;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

