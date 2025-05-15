package com.geekbank.bank.services;

import com.geekbank.bank.models.GifcardClassification;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.repositories.MainScreenGiftCardItemRepository;
import dev.failsafe.internal.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainScreenGiftCardService {

    private final MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository;
    private final KinguinService kinguinService;

    private static final Logger logger = LoggerFactory.getLogger(MainScreenGiftCardService.class);
    @Autowired
    public MainScreenGiftCardService(MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository, KinguinService kinguinService) {
        this.mainScreenGiftCardItemRepository = mainScreenGiftCardItemRepository;
        this.kinguinService = kinguinService;
    }

    @Transactional
    public List<MainScreenGiftCardItemDTO> getMainScreenGiftCardItemsByClassification(GifcardClassification classification) {
        List<MainScreenGiftCardItem> items =  mainScreenGiftCardItemRepository.findByClassification(classification);

        return items.stream().map(
                item -> {
                 KinguinGiftCard giftCard = kinguinService.fetchGiftCardById(String.valueOf(item.getProductId()))
                         .orElse(null);
                 return new MainScreenGiftCardItemDTO(giftCard, item);
                }).toList();

    }

    public Page<MainScreenGiftCardItemDTO> getMainScreenGiftCardItems(Pageable pageable) {
        Page<MainScreenGiftCardItem> pageOfItems = mainScreenGiftCardItemRepository.findAllOrdered(pageable);


        return pageOfItems.map(item -> {
            KinguinGiftCard giftcard = kinguinService.fetchGiftCardById(String.valueOf(item.getProductId()))
                    .orElse(null);
            return new MainScreenGiftCardItemDTO(giftcard, item);
        });
    }

    public MainScreenGiftCardItem addItem(MainScreenGiftCardItem item) {
        System.out.println("productId: " + item.getProductId()); // debe mostrar un valor

        Assert.notNull(item, "Item must not be null");
        item.setProductId((long)item.getProductId());
        Assert.notNull(item.getProductId(), "productId must not be null");

        if (mainScreenGiftCardItemRepository.existsByProductId(item.getProductId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate");
        }

        return mainScreenGiftCardItemRepository.save(item);
    }


    @Transactional
    public void removeItems(List<Long> productIds) {
        logger.debug("Intentando eliminar elementos de tarjetas de regalo con IDs de producto: {}", productIds);

        mainScreenGiftCardItemRepository.deleteByProductIdIn(productIds);

        List<MainScreenGiftCardItem> remainingItems = mainScreenGiftCardItemRepository.findByProductIdIn(productIds);
        if (remainingItems.isEmpty()) {
            logger.debug("Se eliminaron correctamente los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        } else {
            logger.error("No se pudieron eliminar todos los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        }
    }
}