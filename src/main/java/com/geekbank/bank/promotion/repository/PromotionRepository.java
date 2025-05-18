package com.geekbank.bank.promotion.repository;

import com.geekbank.bank.promotion.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findById(Long Id);
    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    void deleteByCode(String code);
}
