package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Orders;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para manejar las órdenes manuales.
 */
@RestController
@RequestMapping("/api/manual-orders")
public class ManualOrdersController {

    @Autowired
    private ManualOrderService manualOrderService;
    @Autowired
    private OrderService orderService;
    private Orders order;
    private Transaction transaction;
    /**
     * Endpoint para ejecutar el script de Selenium.
     * Método: POST
     * URL: /api/manual-orders/run
     *
     * @return Mensaje de estado de la ejecución.
     */
    @PostMapping("/run/{transactionNumber}")
    public ResponseEntity<String> runManualOrder(@PathVariable String transactionNumber) {

        String result = manualOrderService.runManualOrder(transactionNumber);
        if (result.startsWith("Interacción completada")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Ruta de prueba para verificar que el controlador está funcionando.
     * Método: GET
     * URL: /api/manual-orders/test
     *
     * @return Mensaje de prueba.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend de Selenium está funcionando.");
    }
}