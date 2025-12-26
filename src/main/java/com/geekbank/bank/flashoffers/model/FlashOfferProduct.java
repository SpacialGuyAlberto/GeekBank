package com.geekbank.bank.flashoffers.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class FlashOfferProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, name="productId")
    Long productId;
    @Column(unique = true, name="productName")
    String productName;
    @Column(unique = true, name="originalPrice")
    BigDecimal originalPrice;
    @Column(unique = true, name="temporaryPrice")
    BigDecimal temporaryPrice;


}
