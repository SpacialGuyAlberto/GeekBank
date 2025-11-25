package com.geekbank.bank.flashoffers.service;

import com.geekbank.bank.flashoffers.model.FlashOffer;
import com.geekbank.bank.flashoffers.repository.FlashOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlashOfferService {

    private final FlashOfferRepository repository;

    public FlashOfferService(FlashOfferRepository repository) {
        this.repository = repository;
    }

    public List<FlashOffer> getAllOffers() {
        return repository.findAll();
    }

    public Optional<FlashOffer> getOfferById(Long id) {
        return repository.findById(id);
    }

    public Optional<FlashOffer> getOfferByProductId(Long productId) {
        return repository.findByProductId(productId);
    }

    @Transactional
    public FlashOffer createOffer(FlashOffer offer) {
        offer.setCreatedAt(LocalDateTime.now());
        return repository.save(offer);
    }

    @Transactional
    public void deleteOffer(Long id) {
        repository.deleteById(id);
    }
}

