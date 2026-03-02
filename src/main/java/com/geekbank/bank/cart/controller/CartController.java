package com.geekbank.bank.cart.controller;

import com.geekbank.bank.cart.dto.AddCartItemRequest;
import com.geekbank.bank.cart.dto.UpdateCartItemQuantityRequest;
import com.geekbank.bank.cart.model.CartItem;
import com.geekbank.bank.cart.dto.CartItemWithGiftcardDTO;
import com.geekbank.bank.cart.service.CartServiceImpl;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartServiceImpl cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartServiceImpl cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemWithGiftcardDTO>> getCartItems(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(cartService.getCartItems(user));
    }

    @PostMapping
    public ResponseEntity<CartItem> addCartItem(Authentication authentication,
            @RequestBody AddCartItemRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(cartService.addCartItem(user, request));
    }

    @PutMapping()
    public ResponseEntity<Void> updateCartItemQuantity(Authentication authentication,
            @RequestBody UpdateCartItemQuantityRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.updateCartItemQuantity(request, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeAllCartItems(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.removeAllCartItems(user);
        return ResponseEntity.ok().build();
    }
}
