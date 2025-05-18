package com.geekbank.bank.cart.dto;

import com.geekbank.bank.cart.model.CartItem;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemWithGiftcardDTO {
    private CartItem cartItem;
    private KinguinGiftCard giftcard;

    public CartItemWithGiftcardDTO(CartItem cartItem, KinguinGiftCard giftcard) {
        this.cartItem = cartItem;
        this.giftcard = giftcard;
    }
}
