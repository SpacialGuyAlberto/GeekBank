package com.geekbank.bank.models;

public class FreeFireGiftCard implements GiftCard {
    private String cardNumber;
    private String product;
    private double price;
    private String expirationDate;

    public FreeFireGiftCard(String cardNumber, String product, double price, String expirationDate) {
        this.cardNumber = cardNumber;
        this.product = product;
        this.price = price;
        this.expirationDate = expirationDate;
    }

    @Override
    public String getCardNumber() {
        return cardNumber;
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void redeem() {

        System.out.println("Redeeming " + product + " worth " + price + " USD using card " + cardNumber);
    }
}

