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

    private final FlashOfferRepository repository;
    private final FlashOfferProductRepository flashOfferProductRepository;

    public FlashOfferService(FlashOfferRepository repository, FlashOfferProductRepository flashOfferProductRepository) {
        this.repository = repository;
        this.flashOfferProductRepository = flashOfferProductRepository;
    }

    public List<FlashOffer> getAllOffers() {
        return repository.findAll();
    }

    public Optional<FlashOffer> getOfferByProductId(Long productId) {
        return repository.findByProductId(productId);
    }

    @Transactional
    public FlashOffer createOffer(FlashOffer offer) {
        offer.setCreatedAt(LocalDateTime.now());
        offer.getProducts().forEach(product -> {
            flashOfferProductRepository.save(product);
        ;});
        return repository.save(offer);
    }

    @Transactional
    public FlashOffer updateOffer(Long id, FlashOffer offerDetails) {
        FlashOffer offer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flash Offer not found with id " + id));

        offer.setProducts(offerDetails.getProducts());
        offer.setLimitDate(offerDetails.getLimitDate());
        offer.setStatus(offerDetails.getStatus());
        offer.setStockLimit(offerDetails.getStockLimit());
        offer.setUserLimit(offerDetails.getUserLimit());
        offer.setVisibility(offerDetails.getVisibility());
        offer.setAllowedCountries(offerDetails.getAllowedCountries());
        offer.setBadge(offerDetails.getBadge());
        offer.setBannerUrl(offerDetails.getBannerUrl());

        return repository.save(offer);
    }

    @Transactional
    public void deleteOffer(Long id) {
        FlashOffer offer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flash Offer not found with id " + id));
        repository.delete(offer);
    }
}
