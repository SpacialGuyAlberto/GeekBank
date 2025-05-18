package com.geekbank.bank.payment.stripe.controller;

import com.geekbank.bank.payment.stripe.dto.ChargeRequest;
import com.geekbank.bank.payment.stripe.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody ChargeRequest req) {
        try {
            req.setDescription("Ejemplo de cargo");
            req.setCurrency(ChargeRequest.Currency.EUR);
            Charge charge = stripeService.charge(req);
            return ResponseEntity.ok(Map.of(
                    "id", charge.getId(),
                    "status", charge.getStatus(),
                    "chargeId", charge.getId(),
                    "balanceTransaction", charge.getBalanceTransaction()
            ));
        } catch (StripeException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody Map<String, Integer> req) throws StripeException {
        int amount = req.get("amount");
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) amount)
                .setCurrency("eur")
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return Map.of("clientSecret", intent.getClientSecret());
    }
}