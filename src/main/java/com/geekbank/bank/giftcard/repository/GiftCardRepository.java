package com.geekbank.bank.giftcard.repository;

import com.geekbank.bank.giftcard.model.GiftCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCardEntity, Long> {
    // Puedes agregar métodos personalizados si es necesario
    Optional<GiftCardEntity> findById(Long id);

    @Query("SELECT g FROM GiftCardEntity g JOIN Feedback f ON g.kinguinId = f.giftCardId GROUP BY g.kinguinId ORDER BY COUNT(f) DESC")
    List<GiftCardEntity> findTopKPopular(Pageable pageable);

    @Query("SELECT g FROM GiftCardEntity g WHERE g.kinguinId <> :productId")
    List<GiftCardEntity> findAllExcludingId(@Param("productId") Long productId);

    @Query("SELECT g FROM GiftCardEntity g WHERE g.kinguinId <> :productId")
    List<GiftCardEntity> findLimitedProductsExcludingId(@Param("productId") Long productId, Pageable pageable);

//    @Query("SELECT DISTINCT g FROM GiftCardEntity g JOIN g.genres genre WHERE g.kinguinId <> :productId " +
//            "AND genre IN :genres " +
//            "AND g.platform = :platform")
//    List<GiftCardEntity> findSimilarProductsExcludingId(
//            @Param("productId") Long productId,
//            @Param("genres") List<String> genres,
//            @Param("platform") String platform,
//            Pageable pageable);

}

