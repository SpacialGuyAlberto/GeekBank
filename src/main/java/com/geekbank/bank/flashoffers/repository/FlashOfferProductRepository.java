package com.geekbank.bank.flashoffers.repository;

import com.geekbank.bank.flashoffers.model.FlashOfferProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashOfferProductRepository extends JpaRepository<FlashOfferProduct, Long> {

}
