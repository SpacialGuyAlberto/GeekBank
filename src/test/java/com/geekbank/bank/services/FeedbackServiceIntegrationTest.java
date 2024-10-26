package com.geekbank.bank.services;

import com.geekbank.bank.dto.FeedbackRequest;
import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.FeedBackRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FeedbackServiceIntegrationTest {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedBackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        feedbackRepository.deleteAll();
        userRepository.deleteAll();

        // Crear un usuario para asociar con el feedback
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setPassword("{bcrypt}$2a$10$...");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Crear un nuevo feedback")
    public void testCreateFeedback() {
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        feedbackRequest.setUserId(testUser.getId());
        feedbackRequest.setGiftCardId(100L);
        feedbackRequest.setScore(5);
        feedbackRequest.setMessage("Excelente servicio");

        Feedback createdFeedback = feedbackService.createFeedback(feedbackRequest);

        assertNotNull(createdFeedback.getId(), "El feedback debería tener un ID después de guardarse");
        assertEquals(testUser.getId(), createdFeedback.getUserId(), "El ID de usuario debería coincidir");
        assertEquals(5, createdFeedback.getScore(), "La calificación debería ser 5");
        assertEquals("Excelente servicio", createdFeedback.getMessage(), "El mensaje debería coincidir");
    }

    @Test
    @DisplayName("Obtener feedback por ID")
    public void testGetFeedbackById() {
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        feedbackRequest.setUserId(testUser.getId());
        feedbackRequest.setGiftCardId(100L);
        feedbackRequest.setScore(4);
        feedbackRequest.setMessage("Buen servicio");

        Feedback feedback = feedbackService.createFeedback(feedbackRequest);

        Optional<Feedback> retrievedFeedback = feedbackService.getFeedbackById(feedback.getId());
        assertTrue(retrievedFeedback.isPresent(), "El feedback debería estar presente en la base de datos");
        assertEquals("Buen servicio", retrievedFeedback.get().getMessage(), "El mensaje debería coincidir");
    }

    @Test
    @DisplayName("Obtener todos los feedbacks")
    public void testGetAllFeedbacks() {
        feedbackService.createFeedback(new FeedbackRequest(testUser.getId(), 100L, 5, "Excelente"));
        feedbackService.createFeedback(new FeedbackRequest(testUser.getId(), 101L, 4, "Bueno"));

        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();

        assertEquals(2, feedbacks.size(), "Debería haber dos feedbacks en la base de datos");
    }

    @Test
    @DisplayName("Obtener feedbacks por rango de fechas")
    public void testGetFeedbacksInDateRange() {
        FeedbackRequest feedbackRequest1 = new FeedbackRequest(testUser.getId(), 100L, 4, "Servicio aceptable");
        FeedbackRequest feedbackRequest2 = new FeedbackRequest(testUser.getId(), 101L, 5, "Servicio excelente");

        Feedback feedback1 = feedbackService.createFeedback(feedbackRequest1);
        Feedback feedback2 = feedbackService.createFeedback(feedbackRequest2);

        Date startDate = new Date(feedback1.getCreatedAt().getTime() - 1000);
        Date endDate = new Date(feedback2.getCreatedAt().getTime() + 1000);

        List<Feedback> feedbacks = feedbackService.getFeedbacksInDateRange(startDate, endDate);

        assertEquals(2, feedbacks.size(), "Debería haber dos feedbacks en el rango de fechas");
    }

    @Test
    @DisplayName("Eliminar un feedback")
    public void testDeleteFeedback() {
        FeedbackRequest feedbackRequest = new FeedbackRequest(testUser.getId(), 100L, 3, "No fue lo esperado");

        Feedback feedback = feedbackService.createFeedback(feedbackRequest);

        feedbackService.deleteFeedback(feedback.getId());
        Optional<Feedback> deletedFeedback = feedbackService.getFeedbackById(feedback.getId());

        assertFalse(deletedFeedback.isPresent(), "El feedback debería eliminarse de la base de datos");
    }
}
