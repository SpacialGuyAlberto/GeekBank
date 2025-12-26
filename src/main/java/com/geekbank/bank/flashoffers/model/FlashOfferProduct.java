package com.geekbank.bank.flashoffers.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_offer_products")
@Getter
@Setter
public class FlashOfferProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;

    private BigDecimal originalPrice;
    private BigDecimal temporaryPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_offer_id", nullable = false)
    private FlashOffer flashOffer;
}

