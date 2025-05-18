package com.geekbank.bank.giftcard.featured.highlight.dto;

import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.featured.highlight.model.HighlightItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HighlightItemWithGiftcardDTO {
    private HighlightItem highlightItem;
    private KinguinGiftCard giftcard;

    public HighlightItemWithGiftcardDTO(HighlightItem highlightItem, KinguinGiftCard giftcard) {
        this.highlightItem = highlightItem;
        this.giftcard = giftcard;
    }

}
