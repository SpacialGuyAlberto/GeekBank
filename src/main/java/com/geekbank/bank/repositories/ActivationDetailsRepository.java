package com.geekbank.bank.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.geekbank.bank.models.ActivationDetails;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ActivationDetailsRepository extends JpaRepository<ActivationDetails, Long> {
    Optional<ActivationDetails> findByKinguinId(Long kinguinId);
    void deleteByKinguinId(Long kinguinId);

    // Método personalizado para actualizar texto y URL
    @Modifying
    @Transactional
    @Query("UPDATE ActivationDetails a SET a.textDetails = :textDetails, a.videoUrl = :videoUrl WHERE a.kinguinId = :kinguinId")
    int updateActivationDetails(@Param("kinguinId") Long kinguinId,
                                @Param("textDetails") String textDetails,
                                @Param("videoUrl") String videoUrl);
}

