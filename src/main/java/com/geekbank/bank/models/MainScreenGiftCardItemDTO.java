package com.geekbank.bank.models;

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
