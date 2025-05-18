package com.geekbank.bank.support.feedback.controller;

import com.geekbank.bank.support.feedback.model.Feedback;
import com.geekbank.bank.support.feedback.dto.FeedbackRequest;
import com.geekbank.bank.support.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public List<Feedback> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        Optional<Feedback> feedback = feedbackService.getFeedbackById(id);
        return feedback.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackService.createFeedback(feedbackRequest);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/giftcard/{giftCardId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByGiftCardId(@PathVariable Long giftCardId) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByGiftCardId(giftCardId);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByUserId(@PathVariable  Long userId) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByUserId(userId);
        return ResponseEntity.ok(feedbacks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/after/{date}")
    public ResponseEntity<List<Feedback>> getFeedbacksAfterDate(@PathVariable long date) {
        Date limitDate = new Date(date);
        List<Feedback> feedbacks = feedbackService.getFeedbacksAfterDate(limitDate);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/before/{date}")
    public ResponseEntity<List<Feedback>> getFeedbacksBeforeDate(@PathVariable long date) {
        Date limitDate = new Date(date);
        List<Feedback> feedbacks = feedbackService.getFeedbacksBeforeDate(limitDate);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/range")
    public ResponseEntity<List<Feedback>> getFeedbacksInDateRange(@RequestParam long startDate,
                                                                  @RequestParam long endDate) {
        Date start = new Date(startDate);
        Date end = new Date(endDate);
        List<Feedback> feedbacks = feedbackService.getFeedbacksInDateRange(start, end);
        return ResponseEntity.ok(feedbacks);
    }

    @GetMapping("/custom-range")
    public ResponseEntity<List<Feedback>> getFeedbacksInDateRangeCustom(@RequestParam long startDate,
                                                                        @RequestParam long endDate) {
        Date start = new Date(startDate);
        Date end = new Date(endDate);
        List<Feedback> feedbacks = feedbackService.getFeedbacksInDateRangeCustom(start, end);
        return ResponseEntity.ok(feedbacks);
    }
}


