package com.geekbank.bank.metrics.sales.controller;
import com.geekbank.bank.metrics.sales.model.SalesMetrics;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.metrics.sales.service.SalesMetricsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics")
public class SalesMetricsController {

    private final SalesMetricsService salesMetricsService;

    public SalesMetricsController(SalesMetricsService salesMetricsService) {
        this.salesMetricsService = salesMetricsService;
    }

    /**
     * Endpoint para obtener las métricas actuales de ventas.
     * GET /api/metrics
     */
    @GetMapping
    public ResponseEntity<SalesMetrics> getCurrentMetrics() {
        SalesMetrics metrics = salesMetricsService.getCurrentMetrics();
        return new ResponseEntity<>(metrics, HttpStatus.OK);
    }

    /**
     * Endpoint para registrar una transacción completada.
     * POST /api/metrics/transaction
     *
     * Body esperado:
     * {
     *   "amountHnl": 1000.0,
     *   "amountUsd": 40.0
     * }
     */
    @PostMapping("/transaction")
    public ResponseEntity<String> recordTransaction(@RequestBody Transaction completedTransaction) {
        salesMetricsService.onTransactionCompleted(completedTransaction);
        return new ResponseEntity<>("Transacción registrada exitosamente.", HttpStatus.CREATED);
    }

    /**
     * (Opcional) Endpoint para resetear las métricas a cero.
     * POST /api/metrics/reset
     */
//    @PostMapping("/reset")
//    public ResponseEntity<String> resetMetrics() {
//        SalesMetrics metrics = new SalesMetrics();
//        salesMetricsService.resetMetrics(metrics);
//        return new ResponseEntity<>("Métricas reseteadas exitosamente.", HttpStatus.OK);
//    }
}

