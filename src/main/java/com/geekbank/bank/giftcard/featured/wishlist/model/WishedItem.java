package com.geekbank.bank.giftcard.featured.wishlist.model;

import com.geekbank.bank.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class WishedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "product_id")
    private Long productId;
    private int quantity;
    private double price;
}
