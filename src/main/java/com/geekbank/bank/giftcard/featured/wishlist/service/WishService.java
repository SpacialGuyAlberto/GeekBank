package com.geekbank.bank.giftcard.featured.wishlist.service;
import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.giftcard.featured.wishlist.model.WishedItem;
import com.geekbank.bank.giftcard.featured.wishlist.dto.WishedItemGiftcardDTO;
import com.geekbank.bank.giftcard.featured.wishlist.repository.WishedItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class WishService {
    private final WishedItemRepository wishedItemRepository;
    private final KinguinService kinguinService;
    private static final Logger logger = LoggerFactory.getLogger(WishService.class);

    @Autowired
    public WishService(WishedItemRepository wishedItemRepository, KinguinService kinguinService) {
        this.wishedItemRepository = wishedItemRepository;
        this.kinguinService = kinguinService;
    }

    public WishedItemGiftcardDTO getWishedItem(Long id){
        WishedItem wishedItem = wishedItemRepository.findByProductId(id);

        KinguinGiftCard giftCard = kinguinService.fetchGiftCardById(wishedItem.getProductId().toString());
        return new WishedItemGiftcardDTO(wishedItem, giftCard);
    }

    public List<WishedItemGiftcardDTO> getWishedItems(User user) {
        List<WishedItem> wishedItems = wishedItemRepository.findByUser(user);

        List<Long> productIds = wishedItems.stream()
                .map(WishedItem::getProductId)
                .collect(Collectors.toList());

        Map<Long, KinguinGiftCard> giftCardMap = kinguinService.fetchGiftCardsByIds(productIds);

        return wishedItems.stream()
                .map(wishedItem -> {
                    KinguinGiftCard giftcard = giftCardMap.get(wishedItem.getProductId());
                    return new WishedItemGiftcardDTO(wishedItem, giftcard);
                })
                .collect(Collectors.toList());
    }


    public WishedItem addWishedItem(User user, Long productId, int quantity) {
        WishedItem existingItem = wishedItemRepository.findByUserAndProductId(user, productId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            existingItem = new WishedItem();
            existingItem.setUser(user);
            existingItem.setProductId(productId);
            existingItem.setQuantity(quantity);
        }
        return wishedItemRepository.save(existingItem);
    }

    @Transactional
    public void removeWishedItem(Long cartItemId) {
        logger.debug("Attempting to delete cart item with ID: {}", cartItemId);
        wishedItemRepository.deleteByCustomQuery(cartItemId);
        if (!wishedItemRepository.existsById(cartItemId)) {
            logger.debug("Successfully deleted cart item with ID: {}", cartItemId);
        } else {
            logger.error("Failed to delete cart item with ID: {}", cartItemId);
        }
    }

    public void removeAllWishedItems(User user) {
        logger.debug("Attempting to delete all wished items for user: {}", user.getEmail());
        wishedItemRepository.deleteAllByUser(user);
        List<WishedItem> remainingItems = wishedItemRepository.findByUser(user);
        if (remainingItems.isEmpty()) {
            logger.debug("Successfully deleted all cart items for user: {}", user.getEmail());
        } else {
            logger.error("Failed to delete all cart items for user: {}", user.getEmail());
        }
    }

    public void updateWishedItemQuantity(Long productId, int quantity, User user) {
        logger.debug("Updating quantity of product ID: {} to {}", productId, quantity);
        wishedItemRepository.updateQuantityByProductIdAndUser(productId, quantity, user);
    }

}
