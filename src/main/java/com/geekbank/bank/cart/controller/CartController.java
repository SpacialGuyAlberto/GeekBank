package com.geekbank.bank.cart.controller;

import com.geekbank.bank.cart.model.CartItem;
import com.geekbank.bank.cart.dto.CartItemWithGiftcardDTO;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.cart.service.CartService;
import com.geekbank.bank.user.service.UserService;
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
    public ResponseEntity<List<CartItemWithGiftcardDTO>> getCartItems(Authentication authentication) {
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
        return ResponseEntity.ok(cartService.addCartItem(user, request.getProductId(), request.getQuantity(), request.getPrice()));
    }

    @PutMapping()
    public ResponseEntity<Void> updateCartItemQuantity(Authentication authentication, @RequestBody UpdateCartItemQuantityRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        cartService.updateCartItemQuantity(request.getProductId(), request.getQuantity(), user);
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


    public static class AddCartItemRequest {
        private Long productId;
        private int quantity;
        private double price;

        public Long getProductId() {
            return this.productId;
        }

        public int getQuantity() {
            return this.quantity;
        }
        public double getPrice(){ return this.price; }


        // Getters and Setters
    }

    public static class UpdateCartItemQuantityRequest {
        private Long productId;
        private int quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }


}
