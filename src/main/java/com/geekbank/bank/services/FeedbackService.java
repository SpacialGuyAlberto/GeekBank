package com.geekbank.bank.services;

import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.FeedBackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para manejo transaccional
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class FeedbackService {

    @Autowired
    private FeedBackRepository feedbackRepository;

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public Feedback createFeedback(User user, String productId, int score, String message) {
        // Crear una nueva instancia de Feedback
        Feedback feedback = new Feedback();
        feedback.setUser(user);          // Establecer el usuario
        feedback.setProductId(productId); // Establecer el ID del producto
        feedback.setScore(score);         // Establecer el puntaje
        feedback.setMessage(message);     // Establecer el mensaje

        // Guardar la instancia de Feedback en la base de datos
        return feedbackRepository.save(feedback);
    }

    public Optional<Feedback> getFeedbackById(Long id) {
        return feedbackRepository.findById(id);
    }

    public List<Feedback> getFeedbacksByProductId(String productId) {
        return feedbackRepository.findByProductId(productId);
    }

    public List<Feedback> getFeedbacksByUser(User user) {
        return feedbackRepository.findByUser(user);
    }

    public List<Feedback> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId);
    }

    public Feedback addFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public Feedback updateFeedback(Long id, Feedback feedbackDetails) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findById(id);
        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            feedback.setUser(feedbackDetails.getUser());
            feedback.setProductId(feedbackDetails.getProductId());
            feedback.setScore(feedbackDetails.getScore());
            feedback.setMessage(feedbackDetails.getMessage());
            // Puedes actualizar más campos si es necesario
            return feedbackRepository.save(feedback);
        } else {
            throw new RuntimeException("Feedback no encontrado con id " + id);
        }
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}
