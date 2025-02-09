package com.geekbank.bank.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.geekbank.bank.converters.ListToStringConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionNumber;

    @Column(nullable = false)
    private double amountUsd;

    @Column(name = "amount_hnl", nullable = false)
    private double amountHnl;

    @Column(name = "exchange_rate", nullable = false)
    private double exchangeRate;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TransactionStatus status;

    private String description;

    private String orderRequestNumber;

    @Column(nullable = true)
    private String externalOrderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = true)
    private String guestId;

    @Column(nullable = true)
    private Long gameUserId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TransactionProduct> products = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isManual = false;

    @Column(nullable = true)
    private Long tempPin;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    @JsonManagedReference
    private SmsMessage smsMessage;

    @Convert(converter = ListToStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> keys = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "affiliate_id", nullable = true)
    private User affiliate;

    @Column(nullable = true)
    private Double discountApplied;

    @Column(nullable = true)
    private Double commissionEarned;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public double getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(double amount) {
        this.amountUsd = amount;
    }

    public double getAmountHnl() {
        return amountHnl;
    }

    public void setAmountHnl(double amountHnl) {
        this.amountHnl = amountHnl;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderRequestNumber() {
        return orderRequestNumber;
    }

    public void setOrderRequestNumber(String orderRequestNumber) {
        this.orderRequestNumber = orderRequestNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public Long getGameUserId() {
        return gameUserId;
    }

    public void setGameUserId(Long gameUserId) {
        this.gameUserId = gameUserId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<TransactionProduct> getProducts() {
        return products;
    }

    public void setProducts(List<TransactionProduct> products) {
        this.products = products;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

    public Long getTempPin() {
        return tempPin;
    }

    public void setTempPin(Long tempPin) {
        this.tempPin = tempPin;
    }

    public SmsMessage getSmsMessage() {
        return smsMessage;
    }

    public void setSmsMessage(SmsMessage smsMessage) {
        this.smsMessage = smsMessage;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getExternalOrderId() {
        return externalOrderId;
    }

    public void setExternalOrderId(String externalOrderId) {
        this.externalOrderId = externalOrderId;
    }

    public User getAffiliate() {
        return affiliate;
    }

    public void setAffiliate(User affiliate) {
        this.affiliate = affiliate;
    }

    public Double getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(Double discountApplied) {
        this.discountApplied = discountApplied;
    }

    public Double getCommissionEarned() {
        return commissionEarned;
    }

    public void setCommissionEarned(Double commissionEarned) {
        this.commissionEarned = commissionEarned;
    }
}
