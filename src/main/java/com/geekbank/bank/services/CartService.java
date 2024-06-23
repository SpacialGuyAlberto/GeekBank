// src/main/java/com/geekbank/bank/services/CartService.java
package com.geekbank.bank.services;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public CartItem addCartItem(User user, Long productId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
