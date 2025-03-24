package com.geekbank.bank.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class HighlightItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Getter
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Double price;
}
