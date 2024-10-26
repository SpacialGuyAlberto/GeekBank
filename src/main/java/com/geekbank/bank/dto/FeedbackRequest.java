package com.geekbank.bank.dto;

import java.util.Date;

public class FeedbackRequest {
    private Long userId; // Cambiado de Long a String
    private Long giftCardId; // Renombrado de productId a giftCardId
    private int score;
    private String message;
    private Date createdAt; // Añadido el campo createdAt
    // Constructor vacío (necesario para algunos frameworks)
    public FeedbackRequest() {}

    // Nuevo constructor con todos los campos
    public FeedbackRequest(Long userId, Long giftCardId, int score, String message, Date createdAt) {
        this.userId = userId;
        this.giftCardId = giftCardId;
        this.score = score;
        this.message = message;
        this.createdAt = createdAt;
    }

    // Constructor alternativo sin el campo createdAt, si no necesitas inicializarlo siempre
    public FeedbackRequest(Long userId, Long giftCardId, int score, String message) {
        this.userId = userId;
        this.giftCardId = giftCardId;
        this.score = score;
        this.message = message;
    }


    // Getters y setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGiftCardId() {
        return giftCardId;
    }

    public void setGiftCardId(Long giftCardId) {
        this.giftCardId = giftCardId;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setRating(int i) {
        this.score = i;
    }
}
