package com.geekbank.bank.models;

public interface GiftCard {
    boolean isHighlight = false;
    String getCardNumber();
    String getProduct();
    double getPrice();
    String getExpirationDate();
    void redeem();

}
