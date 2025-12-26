package com.geekbank.bank.flashoffers.model;

import com.geekbank.bank.flashoffers.enums.FlashSaleStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flash_offers")
@Getter
@Setter
public class FlashOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "temporary_price")
    BigDecimal temporaryPrice;

    @OneToMany
    @JoinColumn(name = "flash_offer_id") // FK en flash_offer_product
    private List<FlashOfferProduct> products = new ArrayList<>();

    @Column(name = "original_price")
    BigDecimal originalPrice;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "limit_date")
    LocalDateTime limitDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    FlashSaleStatus status;

    @Column(name = "stock_limit")
    Integer stockLimit;

    @Column(name = "user_limit")
    Integer userLimit;

    @Column(name = "visibility")
    String visibility;

    @Column(name = "allowed_countries")
    String allowedCountries;

    @Column(name = "badge")
    String badge;

    @Column(name = "banner_url")
    String bannerUrl;

}
