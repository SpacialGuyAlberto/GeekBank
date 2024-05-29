package com.geekbank.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Transaction {
    @Id
    private long id;
    private String transactionNumber;
    private double amount;
//    private TransactionType type;
//    private LocalDateTime timestamp;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    // Other attributes (e.g., description)

    // Constructors, getters, setters, etc.
}

