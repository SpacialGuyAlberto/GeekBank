package com.geekbank.bank.support.feedback.dto;

import lombok.Data;
import java.util.Date;

@Data
public class FeedbackRequest {
    private Long userId;
    private Long giftCardId;
    private int score;
    private String message;
    private Date createdAt;

    public FeedbackRequest() {}

    public FeedbackRequest(Long userId, Long giftCardId, int score, String message) {
        this.userId = userId;
        this.giftCardId = giftCardId;
        this.score = score;
        this.message = message;
    }

}
