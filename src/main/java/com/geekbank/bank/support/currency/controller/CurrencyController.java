package com.geekbank.bank.support.currency.controller;

import com.geekbank.bank.core.controller.BaseController;
import com.geekbank.bank.core.response.ApiResponse;
import com.geekbank.bank.support.currency.dto.ExchangeRateResponse;
import com.geekbank.bank.support.currency.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController extends BaseController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/exchange-rate")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getExchangeRateUSDtoHNL() {
        double rate = currencyService.getExchangeRateUSDtoHNL();
        return success(new ExchangeRateResponse(rate));
    }
}
