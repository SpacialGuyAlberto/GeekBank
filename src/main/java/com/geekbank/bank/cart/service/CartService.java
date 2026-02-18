package com.geekbank.bank.cart.service;

import com.geekbank.bank.cart.dto.CartItemWithGiftcardDTO;

import java.util.List;

public interface CartService<E, T> {
    public List<E> getCartItems(T t);
}
