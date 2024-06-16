package com.geekbank.bank.services;

import com.geekbank.bank.models.GiftCard;

import java.util.ArrayList;
import java.util.List;

public class GiftCardService {
    private final List<GiftCard> giftCards = new ArrayList<>();

    public void addGiftCard(GiftCard giftCard) {
        giftCards.add(giftCard);
    }

    public GiftCard getGiftCard(String cardNumber) {
        for (GiftCard card : giftCards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    public void redeemGiftCard(String cardNumber) {
        GiftCard card = getGiftCard(cardNumber);
        if (card != null) {
            card.redeem();
        } else {
            throw new IllegalArgumentException("Card not found");
        }
    }
}
