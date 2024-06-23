// src/main/java/com/geekbank/bank/controllers/CartController.java
package com.geekbank.bank.controllers;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.CartService;
import com.geekbank.bank.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCartItems(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getCartItems(user));
    }

    @PostMapping
    public ResponseEntity<CartItem> addCartItem(Authentication authentication, @RequestBody AddCartItemRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.addCartItem(user, request.getProductId(), request.getQuantity()));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    public static class AddCartItemRequest {
        private Long productId;
        private int quantity;

        public Long getProductId() {
            return  this.productId;
        }

        public int getQuantity() {
            return this.quantity;
        }

        // Getters and Setters
    }
}
