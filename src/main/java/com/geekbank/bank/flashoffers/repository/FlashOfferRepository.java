package com.geekbank.bank.flashoffers.repository;

import com.geekbank.bank.flashoffers.model.FlashOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashOfferRepository extends JpaRepository<FlashOffer, Long> {
    Optional<FlashOffer> findByProductId(Long productId);
    List<FlashOffer> findByLimitDate(LocalDateTime now);
}

