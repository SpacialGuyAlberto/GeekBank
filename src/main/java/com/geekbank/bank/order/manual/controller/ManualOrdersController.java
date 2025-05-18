package com.geekbank.bank.order.manual.controller;

import com.geekbank.bank.order.model.Orders;
import com.geekbank.bank.order.manual.service.ManualOrderService;
import com.geekbank.bank.order.service.OrderService;
import com.geekbank.bank.transaction.model.Transaction;
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

    @PostMapping("/run/{transactionNumber}")
    public ResponseEntity<String> runManualOrder(@PathVariable String transactionNumber) {

        String result = manualOrderService.runManualOrder(transactionNumber);
        if (result.startsWith("Interacción completada")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend de Selenium está funcionando.");
    }
}