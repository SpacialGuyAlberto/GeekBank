package com.geekbank.bank.repositories;

import com.geekbank.bank.models.GifcardClassification;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MainScreenGiftCardItemRepository extends JpaRepository<MainScreenGiftCardItem, Long> {

    /**
     * Para la paginación:
     */
    Page<MainScreenGiftCardItem> findAll(Pageable pageable);
    /**
     * Para buscar registros específicos:
     */
    @Query("SELECT i FROM MainScreenGiftCardItem i WHERE i.productId IN :productIds")
    List<MainScreenGiftCardItem> findByProductIdIn(List<Long> productIds);

    Optional<MainScreenGiftCardItem> findById(Long id);
    Optional<MainScreenGiftCardItem> findByProductId(Long id);

    @Query("SELECT m FROM MainScreenGiftCardItem m ORDER BY m.id ASC")
    Page<MainScreenGiftCardItem> findAllOrdered(Pageable pageable);
    /**
     * Para eliminar registros por sus productIds:
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM MainScreenGiftCardItem i WHERE i.productId IN :productIds")
    void deleteByProductIdIn(List<Long> productIds);

    @Override
    void deleteAll();

    @Override
    List<MainScreenGiftCardItem> findAll();

    boolean existsByProductId(Long productId);

    @Transactional
    List<MainScreenGiftCardItem> findByClassification(GifcardClassification classification);

    void deleteByProductId(Long productId);
}
