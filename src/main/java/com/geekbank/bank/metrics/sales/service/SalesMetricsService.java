package com.geekbank.bank.metrics.sales.service;

import com.geekbank.bank.metrics.sales.model.SalesMetrics;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.metrics.sales.repository.SalesMetricsRepository;
import org.springframework.stereotype.Service;

@Service
public class SalesMetricsService {

    private final SalesMetricsRepository metricsRepository;

    public SalesMetricsService(SalesMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    /**
     * Llamado cada vez que se completa una transacción/venta.
     * @param completedTransaction La transacción completada con sus datos (precio, costos, etc.)
     */
    public void onTransactionCompleted(Transaction completedTransaction) {
        // Recuperar métricas actuales
        SalesMetrics currentMetrics = (SalesMetrics) metricsRepository.findById(1L) // por ejemplo un registro único que mantenga métricas acumuladas
                .orElse(new SalesMetrics());

        // Actualizar las métricas
        currentMetrics.incrementSalesCount();
        currentMetrics.addToTotalRevenue(completedTransaction.getAmountHnl());
        // Suponiendo que tienes un costo asociado:
        currentMetrics.addToTotalProfit(completedTransaction.getAmountHnl() - completedTransaction.getAmountUsd());

        // Guardar las métricas actualizadas
        metricsRepository.save(currentMetrics);
    }

    // Métodos para consultar las métricas
    public SalesMetrics getCurrentMetrics() {
        return (SalesMetrics) metricsRepository.findById(1L).orElse(new SalesMetrics());
    }
}

