package com.geekbank.bank.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MainScreenGiftCardItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    @JsonProperty("productId")
    private Long productId;

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // si usas enums por nombre
    private GifcardClassification classification;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}