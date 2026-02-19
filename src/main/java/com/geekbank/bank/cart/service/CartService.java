package com.geekbank.bank.cart.service;

import java.util.List;

public interface CartService<E, T> {
    public List<E> getCartItems(T t);
}
