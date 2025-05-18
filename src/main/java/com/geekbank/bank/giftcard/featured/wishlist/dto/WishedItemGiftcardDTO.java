package com.geekbank.bank.giftcard.featured.wishlist.dto;

import com.geekbank.bank.giftcard.featured.wishlist.model.WishedItem;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;

public class WishedItemGiftcardDTO {
    public WishedItemGiftcardDTO() {

    }

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
