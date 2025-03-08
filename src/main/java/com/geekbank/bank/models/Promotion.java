package com.geekbank.bank.models;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
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
