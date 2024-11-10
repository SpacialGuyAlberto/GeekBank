package com.geekbank.bank.models;

public class MainScreenGiftCardItemDTO {

    private MainScreenGiftCardItem mainScreenGiftCardItem;
    private KinguinGiftCard giftcard;

    public MainScreenGiftCardItemDTO(MainScreenGiftCardItem mainScreenGiftCardItem, KinguinGiftCard giftcard) {
        this.mainScreenGiftCardItem = mainScreenGiftCardItem;
        this.giftcard = giftcard;
    }

    public MainScreenGiftCardItemDTO() {
    }

    public MainScreenGiftCardItem getMainScreenGiftCardItem() {
        return mainScreenGiftCardItem;
    }

    public void setMainScreenGiftCardItem(MainScreenGiftCardItem mainScreenGiftCardItem) {
        this.mainScreenGiftCardItem = mainScreenGiftCardItem;
    }

    public KinguinGiftCard getGiftcard() {
        return giftcard;
    }

    public void setGiftcard(KinguinGiftCard giftcard) {
        this.giftcard = giftcard;
    }
}