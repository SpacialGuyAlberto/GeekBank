package com.geekbank.bank.giftcard.constants;

public enum FreeFireDiamondDenomination {
    DIAMONDS_100(100, 24.01),
    DIAMONDS_310(310, 74.56),
    DIAMONDS_520(520, 125.00),
    DIAMONDS_1060(1060, 251.00),
    DIAMONDS_2180(2180, 478.00),
    DIAMONDS_5600(5600, 1199.31);

    private final int quantity;
    private final double price;

    FreeFireDiamondDenomination(int quantity, double price) {
        this.quantity = quantity;
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
