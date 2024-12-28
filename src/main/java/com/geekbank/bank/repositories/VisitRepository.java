package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findTopBySessionIdOrderByTimestampDesc(String sessionId);
}
