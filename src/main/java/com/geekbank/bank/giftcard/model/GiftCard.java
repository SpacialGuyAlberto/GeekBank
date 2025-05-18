package com.geekbank.bank.giftcard.model;

public interface GiftCard {
    boolean isHighlight = false;
    String getCardNumber();
    String getProduct();
    double getPrice();
    String getExpirationDate();
    void redeem();

}
