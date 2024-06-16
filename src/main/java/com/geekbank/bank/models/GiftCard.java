package com.geekbank.bank.models;

public interface GiftCard {
    String getCardNumber();
    String getProduct();
    double getPrice();
    String getExpirationDate();
    void redeem();
}
