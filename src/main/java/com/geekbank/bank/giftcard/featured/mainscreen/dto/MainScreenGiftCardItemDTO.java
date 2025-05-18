package com.geekbank.bank.giftcard.featured.mainscreen.dto;

import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.featured.mainscreen.model.MainScreenGiftCardItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainScreenGiftCardItemDTO {
    private KinguinGiftCard giftcard;
    private MainScreenGiftCardItem mainScreenGiftCardItem;
}
