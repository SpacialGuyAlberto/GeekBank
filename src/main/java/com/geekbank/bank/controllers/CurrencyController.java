// src/main/java/com/geekbank/bank/controllers/CurrencyController.java

package com.geekbank.bank.controllers;

import com.geekbank.bank.services.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/exchange-rate")
    public ExchangeRateResponse getExchangeRateUSDtoHNL() {
        double rate = currencyService.getExchangeRateUSDtoHNL();
        return new ExchangeRateResponse(rate);
    }

    public static class ExchangeRateResponse {
        private double exchangeRate;

        public ExchangeRateResponse(double exchangeRate) {
            this.exchangeRate = exchangeRate;
        }

        public double getExchangeRate() {
            return exchangeRate;
        }

        public void setExchangeRate(double exchangeRate) {
            this.exchangeRate = exchangeRate;
        }
    }
}
