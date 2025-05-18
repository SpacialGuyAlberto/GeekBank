package com.geekbank.bank.metrics.sales.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SalesMetrics {
    @Id
    private Long id = 1L;
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

}
