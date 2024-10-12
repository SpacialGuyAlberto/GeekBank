package com.geekbank.bank.models;

import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @Column(name = "product_id")
    private String productId; // Mismo ID que en KinguinGiftCard

    private String name;
    private double price;
    private String description;

    // Constructor vacío requerido por JPA
    public Product() {}

    // Constructor con parámetros
    public Product(String productId, String name, double price, String description) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Getters y Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return this.description;
    }

    // Otros getters y setters para 'name', 'price' y 'description'
    // ...
}
