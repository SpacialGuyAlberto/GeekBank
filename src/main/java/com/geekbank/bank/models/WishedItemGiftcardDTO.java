package com.geekbank.bank.models;

public class WishedItemGiftcardDTO {
    public WishedItem getWishedItem() {
        return wishedItem;
    }

    public void setWishedItem(WishedItem wishedItem) {
        this.wishedItem = wishedItem;
    }

    private WishedItem wishedItem;

    public KinguinGiftCard getGiftCard() {
        return giftCard;
    }

    public void setGiftCard(KinguinGiftCard giftCard) {
        this.giftCard = giftCard;
    }

    private KinguinGiftCard giftCard;

    public WishedItemGiftcardDTO(WishedItem wishedItem, KinguinGiftCard giftCard){
        this.giftCard = giftCard;
        this.wishedItem = wishedItem;
    }


}
