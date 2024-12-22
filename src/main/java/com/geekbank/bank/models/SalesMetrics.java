package com.geekbank.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SalesMetrics {
    @Id
    private Long id = 1L; // un solo registro (o puedes tener múltiples si segmentas por fecha o categoría)
    private long totalSalesCount;
    private double totalRevenue;
    private double totalProfit;

    public void incrementSalesCount() {
        this.totalSalesCount++;
    }

    public void addToTotalRevenue(double amount) {
        this.totalRevenue += amount;
    }

    public void addToTotalProfit(double amount) {
        this.totalProfit += amount;
    }

    // Getters y setters
}
