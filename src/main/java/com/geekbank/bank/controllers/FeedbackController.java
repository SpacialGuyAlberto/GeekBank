package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.dto.FeedbackRequest;
import com.geekbank.bank.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // Obtener todos los feedbacks
    @GetMapping
    public List<Feedback> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    // Obtener feedback por ID
    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
        Optional<Feedback> feedback = feedbackService.getFeedbackById(id);
        return feedback.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo feedback
    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackService.createFeedback(feedbackRequest);
        return ResponseEntity.ok(feedback);
    }

    // Obtener feedbacks por productId
    @GetMapping("/giftcard/{giftCardId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByGiftCardId(@PathVariable Long giftCardId) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByGiftCardId(giftCardId);
        return ResponseEntity.ok(feedbacks);
    }

    // Obtener feedbacks por User (Aquí debes pasar el ID del User)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByUserId(@PathVariable  Long userId) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksByUserId(userId);
        return ResponseEntity.ok(feedbacks);
    }

    // Eliminar un feedback por ID
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


