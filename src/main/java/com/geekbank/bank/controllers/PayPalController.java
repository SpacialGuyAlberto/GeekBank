package com.geekbank.bank.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.services.PayPalService;

import com.paypal.sdk.exceptions.ApiException;
import org.apache.http.HttpException;
import org.springframework.http.HttpStatus;
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

    /* ------------------------- 1. CREATE ORDER ------------------------- */
    /* ------------------------- 1. CREATE ORDER ------------------------- */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, String> req) {
        try {
            String amount = req.getOrDefault("amount", "0.00");
            // El nuevo servicio siempre usa intent = CAPTURE ⇒ no necesitamos pasar nada más
            Map<String, Object> order = payPalService.createOrder(amount);
            return ResponseEntity.ok(order);
        } catch (IOException | ApiException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /* ------------------------- 2. CAPTURE ORDER ------------------------ */
    /* 2 – capture order */
    /* -------------------------- 2· CAPTURE ORDER -------------------------- */
    /* -------------------------- 2 · CAPTURE ORDER -------------------------- */
    @PostMapping("/capture-order/{orderId}")
    public ResponseEntity<Map<String, Object>>
    captureOrder(@PathVariable String orderId) throws JsonProcessingException {
        try {
            /* Éxito 200 / 201 ------------------------------------------------ */
            Map<String, Object> ok = payPalService.captureOrder(orderId);
            return ResponseEntity.ok(ok);

        } catch (IOException io) {                   // errores locales (I/O, parseo…)
            return ResponseEntity.internalServerError().build();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }







}
