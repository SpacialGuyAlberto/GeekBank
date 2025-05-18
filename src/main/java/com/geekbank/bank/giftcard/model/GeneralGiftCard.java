package com.geekbank.bank.giftcard.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralGiftCard implements GiftCard {
    private String cardNumber;
    private String product;
    private double price;
    private String expirationDate;

    public GeneralGiftCard(String cardNumber, String product, double price, String expirationDate) {
        this.cardNumber = cardNumber;
        this.product = product;
        this.price = price;
        this.expirationDate = expirationDate;
    }

    @Override
    public void redeem() {

        System.out.println("Redeeming " + product + " worth " + price + " USD using card " + cardNumber);
    }
}

