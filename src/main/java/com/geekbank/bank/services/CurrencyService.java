// src/main/java/com/geekbank/bank/services/CurrencyService.java

package com.geekbank.bank.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CurrencyService {

    @Value("${currency.api.url}")
    private String apiUrl;

    @Value("${currency.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // Constructor
    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene la tasa de cambio de USD a HNL usando la API externa de currencybeacon.com.
     * Cacha el resultado para evitar múltiples llamadas seguidas a la API.
     *
     * @return Tasa de cambio de USD a HNL.
     */
    @Cacheable("exchangeRateUSDtoHNL")
    public double getExchangeRateUSDtoHNL() {
        String url = String.format("%s?from=USD&to=HNL&amount=1&api_key=%s", apiUrl, apiKey);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, Object> responseDetails = (Map<String, Object>) body.get("response");

                if (responseDetails != null) {
                    Double value = (Double) responseDetails.get("value");
                    Double amount = (Double) responseDetails.get("amount");

                    if (value != null && amount != null && amount != 0) {
                        return value / amount; // Devuelve la tasa de cambio
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener la tasa de cambio de USD a HNL: " + e.getMessage());
        }

        return 24.5; // Tasa fija en caso de error
    }

    // Métodos de conversión
    public double convertUsdToHnl(double amountUsd, double exchangeRate) {
        return amountUsd * exchangeRate;
    }

    public double convertHnlToUsd(double amountHnl, double exchangeRate) {
        return amountHnl / exchangeRate;
    }
}
