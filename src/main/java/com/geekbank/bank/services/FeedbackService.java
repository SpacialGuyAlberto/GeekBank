package com.geekbank.bank.services;

import com.geekbank.bank.dto.FeedbackRequest;
import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.FeedBackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class FeedbackService {

    @Autowired
    private FeedBackRepository feedbackRepository;

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Feedback createFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();
        feedback.setUserId(feedbackRequest.getUserId());
        feedback.setGiftCardId(feedbackRequest.getGiftCardId());
        feedback.setScore(feedbackRequest.getScore());
        feedback.setMessage(feedbackRequest.getMessage());
        // `createdAt` se asigna automáticamente con @CreationTimestamp
        return feedbackRepository.save(feedback);
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getFeedbacksByGiftCardId(Long giftCardId) {
        return feedbackRepository.findByGiftCardId(giftCardId);
    }

    public List<Feedback> getFeedbacksAfterDate(Date date) {
        return feedbackRepository.findByCreatedAtAfter(date);
    }
    public List<Feedback> getFeedbacksBeforeDate(Date date) {
        return feedbackRepository.findByCreatedAtBefore(date);
    }
    public List<Feedback> getFeedbacksInDateRange(Date startDate, Date endDate) {
        return feedbackRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<Feedback> getFeedbacksInDateRangeCustom(Date startDate, Date endDate) {
        return feedbackRepository.findFeedbacksInDateRange(startDate, endDate);
    }

    public List<Feedback> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }



    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}
