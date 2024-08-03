package com.geekbank.bank.services;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.CartItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public CartItem addCartItem(User user, Long productId, int quantity, double price) {
        CartItem existingCartItem = cartItemRepository.findByUserAndProductId(user, productId);
        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
        } else {
            existingCartItem = new CartItem();
            existingCartItem.setUser(user);
            existingCartItem.setProductId(productId);
            existingCartItem.setQuantity(quantity);
            existingCartItem.setPrice(price);
        }
        return cartItemRepository.save(existingCartItem);
    }

    @Transactional
    public void removeCartItem(Long cartItemId) {
        logger.debug("Attempting to delete cart item with ID: {}", cartItemId);
        cartItemRepository.deleteByCustomQuery(cartItemId);
        if (!cartItemRepository.existsById(cartItemId)) {
            logger.debug("Successfully deleted cart item with ID: {}", cartItemId);
        } else {
            logger.error("Failed to delete cart item with ID: {}", cartItemId);
        }
    }

    public void removeAllCartItems(User user) {
        logger.debug("Attempting to delete all cart items for user: {}", user.getEmail());
        cartItemRepository.deleteAllByUser(user);
        List<CartItem> remainingItems = cartItemRepository.findByUser(user);
        if (remainingItems.isEmpty()) {
            logger.debug("Successfully deleted all cart items for user: {}", user.getEmail());
        } else {
            logger.error("Failed to delete all cart items for user: {}", user.getEmail());
        }
    }

    public void updateCartItemQuantity(Long productId, int quantity, User user) {
        logger.debug("Updating quantity of product ID: {} to {}", productId, quantity);
        cartItemRepository.updateQuantityByProductIdAndUser(productId, quantity, user);
    }
}
