package com.geekbank.bank.flashoffers.repository;

import com.geekbank.bank.flashoffers.model.FlashOfferProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashOfferProductRepository
        extends JpaRepository<FlashOfferProduct, Long> {

    // Todos los productos de una Flash Sale
    List<FlashOfferProduct> findByFlashOfferId(Long flashOfferId);

    // Buscar por producto externo (Kinguin, etc.)
    List<FlashOfferProduct> findByProductId(Long productId);
}

