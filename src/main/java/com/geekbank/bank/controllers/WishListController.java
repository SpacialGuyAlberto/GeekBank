package com.geekbank.bank.controllers;

import com.geekbank.bank.models.*;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.services.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wish-list")
public class WishListController {
    private final WishService wishService;
    private final UserService userService;

    @Autowired
    public WishListController(WishService wishService, UserService userService) {
        this.wishService = wishService;
        this.userService = userService;
    }

    @GetMapping("/{wishedItemId}")
    public ResponseEntity<WishedItemGiftcardDTO> getWishedItemById(Authentication authentication, @PathVariable Long wishedItemId) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishService.getWishedItem(wishedItemId));
    }

    @GetMapping
    public ResponseEntity<List<WishedItemGiftcardDTO>> getWishedItems(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishService.getWishedItems(user));
    }

    @PostMapping
    public ResponseEntity<WishedItem> addWishedItem(Authentication authentication, @RequestBody WishListController.AddWishedItemRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(wishService.addWishedItem(user, request.getProductId(), request.getQuantity()));
    }

    @PutMapping()
    public ResponseEntity<Void> updateWishedItemQuantity(Authentication authentication, @RequestBody WishListController.UpdateWishedItemQuantityRequest request) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        wishService.updateWishedItemQuantity(request.getProductId(), request.getQuantity(), user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{wishedItemId}")
    public ResponseEntity<Void> removeWishedItem(@PathVariable Long wishedItemId) {
        wishService.removeWishedItem(wishedItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeAllCartItems(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        wishService.removeAllWishedItems(user);
        return ResponseEntity.ok().build();
    }


    public static class AddWishedItemRequest {
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
    }

    public static class UpdateWishedItemQuantityRequest {
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
