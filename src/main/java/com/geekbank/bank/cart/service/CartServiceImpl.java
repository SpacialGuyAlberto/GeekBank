package com.geekbank.bank.cart.service;

import com.geekbank.bank.cart.dto.AddCartItemRequest;
import com.geekbank.bank.cart.dto.UpdateCartItemQuantityRequest;
import com.geekbank.bank.cart.model.CartItem;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.cart.dto.CartItemWithGiftcardDTO;
import com.geekbank.bank.cart.repository.CartItemRepository;
import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class CartServiceImpl implements CartService<CartItemWithGiftcardDTO, User> {

    private final CartItemRepository cartItemRepository;
    private final KinguinService kinguinService;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired

    public CartServiceImpl(CartItemRepository cartItemRepository, KinguinService kinguinService) {
        this.cartItemRepository = cartItemRepository;
        this.kinguinService = kinguinService;
    }

    @Override
    public List<CartItemWithGiftcardDTO> getCartItems(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        return cartItems.stream()
                .map(cartItem -> {
                    KinguinGiftCard giftcard = kinguinService
                            .fetchGiftCardById(String.valueOf(cartItem.getProductId()));
                    return new CartItemWithGiftcardDTO(cartItem, giftcard);
                })
                .collect(Collectors.toList());
    }

    public CartItem addCartItem(User user, AddCartItemRequest request) {
        CartItem existingCartItem = cartItemRepository.findByUserAndProductId(user, request.getProductId());

        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
        } else {
            existingCartItem = new CartItem();
            existingCartItem.setPrice(request.getPrice());
            existingCartItem.setUser(user);
            existingCartItem.setProductId(request.getProductId());
            existingCartItem.setQuantity(request.getQuantity());
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

    public void updateCartItemQuantity(UpdateCartItemQuantityRequest request, User user) {
        logger.debug("Updating quantity of product ID: {} to {}", request.getProductId(), request.getQuantity());
        cartItemRepository.updateQuantityByProductIdAndUser(request.getProductId(), request.getQuantity(), user);
    }

}
