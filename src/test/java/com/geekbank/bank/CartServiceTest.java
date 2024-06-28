package com.geekbank.bank;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static graphql.Assert.assertFalse;
import static graphql.Assert.assertTrue;

@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testRemoveCartItem() {
        User user = userRepository.findByEmail("test@example.com").get();
        CartItem cartItem = cartService.addCartItem(user, 1L, 1);
        Long cartItemId = cartItem.getId();
        cartService.removeCartItem(cartItemId);
        assertFalse(cartService.getCartItems(user).contains(cartItem));
    }

    @Test
    public void testRemoveAllCartItems() {
        User user = userRepository.findByEmail("test@example.com").get();
        cartService.addCartItem(user, 1L, 1);
        cartService.addCartItem(user, 2L, 1);
        cartService.removeAllCartItems(user);
        assertTrue(cartService.getCartItems(user).isEmpty());
    }
}
