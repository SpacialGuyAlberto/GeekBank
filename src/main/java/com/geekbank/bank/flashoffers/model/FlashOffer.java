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

    @OneToMany(
            mappedBy = "flashOffer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<FlashOfferProduct> products = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "limit_date")
    private LocalDateTime limitDate;

    @Enumerated(EnumType.STRING)
    private FlashSaleStatus status;

    private Integer stockLimit;
    private Integer userLimit;
    private String visibility;
    private String allowedCountries;
    private String badge;
    private String bannerUrl;

    /** helper methods */
    public void addProduct(FlashOfferProduct product) {
        products.add(product);
        product.setFlashOffer(this);
    }

    public void removeProduct(FlashOfferProduct product) {
        products.remove(product);
        product.setFlashOffer(null);
    }
}
