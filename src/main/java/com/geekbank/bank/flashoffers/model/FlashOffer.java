package com.geekbank.bank.flashoffers.model;

import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="flash_offers")
@Getter
@Setter
public class FlashOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id")
    private Long productId;

    @Column(name="created_at")
    LocalDateTime createdAt;

    @Column(name="limit_date")
    LocalDateTime limitDate;

    @Column(name="temporary_price")
    BigDecimal temporaryPrice;

}
