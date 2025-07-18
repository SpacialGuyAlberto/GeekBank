package com.geekbank.bank.metrics.visits.repository;

import com.geekbank.bank.metrics.visits.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findTopBySessionIdOrderByTimestampDesc(String sessionId);
}
