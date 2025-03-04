package com.geekbank.bank.models;

import jakarta.persistence.*;

@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    private Long id;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public double getDiscountPorcentage() {
        return discountPorcentage;
    }

    public void setDiscountPorcentage(double discountPorcentage) {
        this.discountPorcentage = discountPorcentage;
    }

    @Column(name = "code", nullable = false)
    private String code;

    @Column(nullable = true)
    private double discountPorcentage;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
