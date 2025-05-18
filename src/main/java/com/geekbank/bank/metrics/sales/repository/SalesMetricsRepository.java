package com.geekbank.bank.metrics.sales.repository;

import com.geekbank.bank.metrics.sales.model.SalesMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesMetricsRepository extends JpaRepository<SalesMetrics, Long> {
}


