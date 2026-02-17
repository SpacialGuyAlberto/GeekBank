package com.geekbank.bank.support.currency.dto;

public class ExchangeRateResponse {
    private double exchangeRate;

    public ExchangeRateResponse() {
    }

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
