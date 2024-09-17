package com.geekbank.bank.models;

import jakarta.persistence.Entity;


public class CartItemWithGiftcardDTO {
    private CartItem cartItem;
    private KinguinGiftCard giftcard;

    public CartItemWithGiftcardDTO(CartItem cartItem, KinguinGiftCard giftcard) {
        this.cartItem = cartItem;
        this.giftcard = giftcard;
    }

    // Getters and Setters
    public CartItem getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }

    public KinguinGiftCard getGiftcard() {
        return giftcard;
    }

    public void setGiftcard(KinguinGiftCard giftcard) {
        this.giftcard = giftcard;
    }
}
