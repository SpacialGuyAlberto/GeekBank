package com.geekbank.bank.combo.repository;

import com.geekbank.bank.combo.model.ComboEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<ComboEntity, Long> {
    List<ComboEntity> findByIsActiveTrue();
}
