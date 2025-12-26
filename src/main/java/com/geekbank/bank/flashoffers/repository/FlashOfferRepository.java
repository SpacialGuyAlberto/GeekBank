package com.geekbank.bank.flashoffers.repository;

import com.geekbank.bank.flashoffers.enums.FlashSaleStatus;
import com.geekbank.bank.flashoffers.model.FlashOffer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashOfferRepository extends JpaRepository<FlashOffer, Long> {
    List<FlashOffer> findByStatus(FlashSaleStatus status);

    // Flash sales expiradas
    List<FlashOffer> findByLimitDateBefore(LocalDateTime now);

    // Flash sales vigentes ahora
    List<FlashOffer> findByCreatedAtBeforeAndLimitDateAfter(
            LocalDateTime now1,
            LocalDateTime now2
    );
}

