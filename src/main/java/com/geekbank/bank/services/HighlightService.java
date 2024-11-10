package com.geekbank.bank.services;

import com.geekbank.bank.models.HighlightItem;
import com.geekbank.bank.models.HighlightItemWithGiftcardDTO;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.repositories.HighlightItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HighlightService {

    private final HighlightItemRepository highlightItemRepository;
    private final KinguinService kinguinService;

    private static final Logger logger = LoggerFactory.getLogger(HighlightService.class);

    @Autowired
    public HighlightService(HighlightItemRepository highlightItemRepository, KinguinService kinguinService) {
        this.highlightItemRepository = highlightItemRepository;
        this.kinguinService = kinguinService;
    }

    public List<HighlightItemWithGiftcardDTO> getHighlightsByProductIds() {
        List<HighlightItem> highlightItems = highlightItemRepository.findAll();
        return highlightItems.stream()
                .map(highlightItem -> {
                    KinguinGiftCard giftcard = kinguinService.fetchGiftCardById(String.valueOf(highlightItem.getProductId()))
                            .orElse(null);
                    return new HighlightItemWithGiftcardDTO(highlightItem, giftcard);
                })
                .collect(Collectors.toList());
    }

    public List<HighlightItem> addHighlightItems(List<Long> productIds) {
        List<HighlightItem> newHighlights = productIds.stream()
                .map(productId -> {
                    HighlightItem highlightItem = new HighlightItem();
                    highlightItem.setProductId(productId);
                    return highlightItemRepository.save(highlightItem);
                })
                .collect(Collectors.toList());
        return newHighlights;
    }

    @Transactional
    public void removeHighlightItems(List<Long> productIds) {
        logger.debug("Attempting to delete highlight items with product IDs: {}", productIds);
        highlightItemRepository.deleteAll();
        List<HighlightItem> remainingItems = highlightItemRepository.findByProductIdIn(productIds);
        if (remainingItems.isEmpty()) {
            logger.debug("Successfully deleted highlight items for product IDs: {}", productIds);
        } else {
            logger.error("Failed to delete all highlight items for product IDs: {}", productIds);
        }
    }
}
