package com.geekbank.bank.giftcard.featured.highlight.service;

import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.giftcard.featured.highlight.model.HighlightItem;
import com.geekbank.bank.giftcard.featured.highlight.repository.HighlightItemRepository;
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

    public List<HighlightItem> fetchHighlights() {
        List<HighlightItem> highlightItems = highlightItemRepository.findAll();
        return highlightItems;
    }


    public List<HighlightItem> addHighlightItems(List<HighlightItem> highlightRequest) {

        return highlightRequest.stream()
                .map(highlightDTO1 -> {
                    HighlightItem highlightItem = new HighlightItem();
                    highlightItem.setImageUrl(highlightDTO1.getImageUrl());
                    highlightItem.setTitle(highlightDTO1.getTitle());
                    highlightItem.setPrice(highlightDTO1.getPrice());
                    highlightItem.setProductId(highlightDTO1.getProductId());
                    return highlightItemRepository.save(highlightItem);
                })
                .collect(Collectors.toList());
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
