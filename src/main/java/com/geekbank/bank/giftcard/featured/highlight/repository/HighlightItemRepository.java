package com.geekbank.bank.giftcard.featured.highlight.repository;

import com.geekbank.bank.giftcard.featured.highlight.model.HighlightItem;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightItemRepository extends JpaRepository<HighlightItem, Long> {

    List<HighlightItem> findByProductIdIn(List<Long> productIds);

    @Override
    void deleteAll();


    @NotNull
    @Override
    List<HighlightItem> findAll();

    @Transactional
    @Modifying
    @Query("DELETE FROM HighlightItem i WHERE i.productId IN :productIds")
    void deleteByProductIdIn(List<Long> productIds);
}
