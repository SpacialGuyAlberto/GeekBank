package com.geekbank.bank.support.currency.dto;


import lombok.Data;

@Data
public class ExchangeRateResponse {
    private double exchangeRate;

    public ExchangeRateResponse() {
    }

    public ExchangeRateResponse(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

}
