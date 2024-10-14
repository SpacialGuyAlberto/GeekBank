package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedbacks")
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
    public Feedback createFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        // Aquí deberás buscar o crear el User según sea necesario. En este ejemplo, asumo que ya tienes el User.
        User user = new User(); // Recuperar o crear el objeto User según tus necesidades
        user.setId(feedbackRequest.getUserId()); // Asignar el UserId

        return feedbackService.createFeedback(user, feedbackRequest.getProductId(), feedbackRequest.getScore(), feedbackRequest.getMessage());
    }

    // Obtener feedbacks por productId
    @GetMapping("/product/{productId}")
    public List<Feedback> getFeedbacksByProductId(@PathVariable String productId) {
        return feedbackService.getFeedbacksByProductId(productId);
    }

    // Obtener feedbacks por User (Aquí debes pasar el ID del User)
    @GetMapping("/user/{userId}")
    public List<Feedback> getFeedbacksByUserId(@PathVariable Long userId) {
        return feedbackService.getFeedbacksByUserId(userId);
    }

    // Actualizar un feedback existente
    @PutMapping("/{id}")
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long id, @RequestBody Feedback feedbackDetails) {
        try {
            Feedback updatedFeedback = feedbackService.updateFeedback(id, feedbackDetails);
            return ResponseEntity.ok(updatedFeedback);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar un feedback por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}


class FeedbackRequest {
    private Long userId;
    private String productId;
    private int score;
    private String message;

    // Getters y setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
