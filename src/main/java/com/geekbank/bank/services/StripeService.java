package com.geekbank.bank.services;

import com.geekbank.bank.models.ChargeRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${STRIPE_API_KEY}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public Charge charge(ChargeRequest req) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", req.getAmount());
        params.put("currency", req.getCurrency());
        params.put("description", req.getDescription());
        params.put("source", req.getStripeToken());
        return Charge.create(params);
    }
}
