package com.geekbank.bank.models;

import jakarta.persistence.Entity;



public class HighlightItemWithGiftcardDTO {
    private HighlightItem highlightItem;
    private KinguinGiftCard giftcard;

    public HighlightItemWithGiftcardDTO(HighlightItem highlightItem, KinguinGiftCard giftcard) {
        this.highlightItem = highlightItem;
        this.giftcard = giftcard;
    }

    public HighlightItemWithGiftcardDTO() {

    }

    // Getters and Setters
    public HighlightItem getHighlightItem() {
        return this.highlightItem;
    }

    public void setHighlightItem(HighlightItem highlightItem) {
        this.highlightItem = highlightItem;
    }

    public KinguinGiftCard getGiftcard() {
        return giftcard;
    }

    public void setGiftcard(KinguinGiftCard giftcard) {
        this.giftcard = giftcard;
    }
}
