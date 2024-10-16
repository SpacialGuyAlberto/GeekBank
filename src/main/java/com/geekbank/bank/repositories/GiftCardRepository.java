package com.geekbank.bank.repositories;

import com.geekbank.bank.models.GiftCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiftCardRepository extends JpaRepository<GiftCardEntity, Long> {
    // Puedes agregar métodos personalizados si es necesario
    Optional<GiftCardEntity> findById(Long id);

}

