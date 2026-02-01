package com.geekbank.bank.combo.model;

import com.geekbank.bank.giftcard.model.GiftCardEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "combos")
@Getter
@Setter
public class ComboEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany
    @JoinTable(name = "combo_products", joinColumns = @JoinColumn(name = "combo_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<GiftCardEntity> products;

    @Transient
    private Double profitMargin;
}
