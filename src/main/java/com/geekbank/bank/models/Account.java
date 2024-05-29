package com.geekbank.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    private long id;
    private String accountNumber;
    //    private AccountType type;
    private double balance;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

//    public void setType(AccountType type) {
//        this.type = type;
//    }
//
//    public AccountType getType() {
//        return type;
//    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }
}
