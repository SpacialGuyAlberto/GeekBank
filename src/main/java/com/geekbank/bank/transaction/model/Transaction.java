package com.geekbank.bank.transaction.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.geekbank.bank.support.converters.ListToStringConverter;
import com.geekbank.bank.support.sms.model.SmsMessage;
import com.geekbank.bank.transaction.constants.TransactionStatus;
import com.geekbank.bank.transaction.constants.TransactionType;
import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Transaction {

    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false, unique = true)
    private String transactionNumber;

    @Setter
    @Getter
    @Column(nullable = false)
    private double amountUsd;

    @Setter
    @Getter
    @Column(name = "amount_hnl", nullable = false)
    private double amountHnl;

    @Setter
    @Getter
    @Column(name = "exchange_rate", nullable = false)
    private double exchangeRate;

    @Setter
    @Getter
    @Column(nullable = false)
    private String phoneNumber;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Setter
    @Getter
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TransactionStatus status;

    @Setter
    @Getter
    private String description;

    @Setter
    @Getter
    private String orderRequestNumber;

    @Column(nullable = true)
    private String externalOrderId;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Setter
    @Getter
    @Column(nullable = true)
    private String guestId;

    @Getter
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

    public Boolean getManual() {
        return isManual;
    }

    public void setManual(Boolean manual) {
        isManual = manual;
    }

}
