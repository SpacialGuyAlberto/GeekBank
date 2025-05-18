package com.geekbank.bank.support.feedback.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Entity
@Table(name = "feedbacks")
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    private String message;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "giftCardId", nullable = false)
    private Long giftCardId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Feedback() {
    }
}
