package com.geekbank.bank.support.currency;
import com.geekbank.bank.config.PricingConfig;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private final PricingConfig pricingConfig;

    public PricingService(PricingConfig pricingConfig) {
        this.pricingConfig = pricingConfig;
    }

    /**
     * Calcula el precio final aplicando el margen global al precio base.
     * @param basePrice precio base del producto
     * @return precio final con el margen aplicado
     */
    public double calculateSellingPrice(double basePrice) {
        double margin = pricingConfig.getDefaultProfitMargin();
        return basePrice + (basePrice * margin);
    }
}

