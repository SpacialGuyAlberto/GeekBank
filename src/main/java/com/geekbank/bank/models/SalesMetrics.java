package com.geekbank.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SalesMetrics {
    @Id
    private Long id = 1L; // un solo registro (o puedes tener múltiples si segmentas por fecha o categoría)

    public long getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(long totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

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
