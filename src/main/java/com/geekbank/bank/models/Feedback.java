package com.geekbank.bank.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.List;
import java.util.Date;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;


@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference // Para manejar la serialización
    private User user;

    private String productId;
    private int score;
    private String message;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @CreationTimestamp // Anotación para gestionar automáticamente la fecha de creación
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;


    // Constructor sin argumentos (no-arg constructor)
    public Feedback() {
    }

    // Constructor con argumentos
    public Feedback(User user, int score, String message, String productId, Date createdAt) {
        this.user = user;
        this.score = score;
        this.message = message;
        this.productId = productId;
        this.createdAt = createdAt;
    }

    // Getters y setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
