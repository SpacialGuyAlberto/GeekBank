package com.geekbank.bank.controllers;

import com.geekbank.bank.services.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> request) throws IOException {
        String amount = request.get("amount");
        Map<String, Object> order = payPalService.createOrder(amount);
        return ResponseEntity.ok(order);
    }


    @PostMapping("/capture-order/{orderId}")
    public Map<String, Object> captureOrder(@PathVariable String orderId) throws IOException {
        return payPalService.captureOrder(orderId);
    }
}

