package com.geekbank.bank.controllers;

import com.geekbank.bank.services.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> request) {
        try {
            String amount = request.get("amount");
            Map<String, Object> order = payPalService.createOrder(amount);
            return ResponseEntity.ok(order);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) { // Para ApiException y otros
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/capture-order/{orderId}")
    public ResponseEntity<Map<String, Object>> captureOrder(@PathVariable String orderId) {
        try {
            Map<String, Object> result = payPalService.captureOrder(orderId);
            return ResponseEntity.ok(result);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) { // Para ApiException y otros
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
