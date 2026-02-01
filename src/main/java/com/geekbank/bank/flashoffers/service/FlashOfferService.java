package com.geekbank.bank.flashoffers.service;

import com.geekbank.bank.flashoffers.model.FlashOffer;
import com.geekbank.bank.flashoffers.model.FlashOfferProduct;
import com.geekbank.bank.flashoffers.repository.FlashOfferProductRepository;
import com.geekbank.bank.flashoffers.repository.FlashOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlashOfferService {

    private final FlashOfferRepository flashOfferRepository;
    private final FlashOfferProductRepository productRepository;

    public FlashOfferService(
            FlashOfferRepository flashOfferRepository,
            FlashOfferProductRepository productRepository
    ) {
        this.flashOfferRepository = flashOfferRepository;
        this.productRepository = productRepository;
    }

    /* ===================== GET ===================== */

    public List<FlashOffer> getAllOffers() {
        return flashOfferRepository.findAll();
    }

    public List<FlashOfferProduct> findAllProducts() {
        return productRepository.findAll();
    }

    public FlashOffer getOffer(Long id) {
        return flashOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FlashOffer not found"));
    }

    /* ===================== CREATE ===================== */

    @Transactional
    public FlashOffer createOffer(FlashOffer offer) {
        offer.setCreatedAt(LocalDateTime.now());

        offer.getProducts().forEach(product ->
                product.setFlashOffer(offer)
        );

        return flashOfferRepository.save(offer);
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public FlashOffer updateOffer(Long id, FlashOffer updated) {

        FlashOffer existing = getOffer(id);

        existing.setLimitDate(updated.getLimitDate());
        existing.setStatus(updated.getStatus());
        existing.setStockLimit(updated.getStockLimit());
        existing.setUserLimit(updated.getUserLimit());
        existing.setVisibility(updated.getVisibility());
        existing.setAllowedCountries(updated.getAllowedCountries());
        existing.setBadge(updated.getBadge());
        existing.setBannerUrl(updated.getBannerUrl());

        /* 🔥 reemplazar productos */
        existing.getProducts().clear();

        updated.getProducts().forEach(product -> {
            product.setFlashOffer(existing);
            existing.getProducts().add(product);
        });

        return flashOfferRepository.save(existing);
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public void deleteOffer(Long id) {
        FlashOffer offer = getOffer(id);
        flashOfferRepository.delete(offer);
    }
}

