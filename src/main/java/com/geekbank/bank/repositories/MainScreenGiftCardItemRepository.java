package com.geekbank.bank.repositories;

import com.geekbank.bank.models.MainScreenGiftCardItem;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * Para eliminar registros por sus productIds:
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM MainScreenGiftCardItem i WHERE i.productId IN :productIds")
    void deleteByProductIdIn(List<Long> productIds);

    // Métodos por defecto de JpaRepository (deleteAll, findAll, etc.)
    @Override
    void deleteAll();

    @Override
    List<MainScreenGiftCardItem> findAll();
}
