package com.geekbank.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PricingConfig {
    @Value("${ecommerce.profit-margin.default}")
    private double defaultProfitMargin;
    public double getDefaultProfitMargin() {
        return defaultProfitMargin;
    }
}

