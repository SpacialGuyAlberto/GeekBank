package com.geekbank.bank.promotion.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.geekbank.bank.user.model.User;
import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

@Entity
@Table(name = "promotion")
@Getter
@Setter
public class Promotion {

    @Id
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(nullable = true)
    private double discountPorcentage;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

}
