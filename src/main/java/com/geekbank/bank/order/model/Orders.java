package com.geekbank.bank.order.model;

import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.transaction.model.TransactionProduct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, unique = true)
    private String orderRequestId;

    @Column(nullable = true)
    private Long userId;

    @Column(nullable = true)
    private String guestId;

    @Column(nullable = true)
    private Long gameUserId;

    @Column(nullable = false)
    private Boolean manual;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = true)
    private String refNumber;

    @Column(nullable = true)
    private String email;



    @Column(nullable = true)
    private Boolean sendKeyToSMS;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = true)
    private Transaction transaction;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionProduct> products = new ArrayList<>();

    public Orders() {
        this.createdAt = LocalDateTime.now();
        this.products = new ArrayList<>();
    }
    public void setProducts(List<TransactionProduct> products) {
        this.products = new ArrayList<>(products); // Crea una nueva lista basada en la existente
    }
}
