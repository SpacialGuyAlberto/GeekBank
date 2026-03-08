package com.geekbank.bank.giftcard.featured.wishlist.dto;

import com.geekbank.bank.giftcard.featured.wishlist.model.WishedItem;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import lombok.Data;

@Data
public class WishedItemGiftcardDTO {
    public WishedItemGiftcardDTO() {

    }
    private WishedItem wishedItem;

    private KinguinGiftCard giftCard;

    public WishedItemGiftcardDTO(WishedItem wishedItem, KinguinGiftCard giftCard){
        this.giftCard = giftCard;
        this.wishedItem = wishedItem;
    }
}
