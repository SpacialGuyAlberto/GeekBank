package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class FeedBackRepositoryIntegrationTest {

    @Autowired
    private FeedBackRepository feedbackRepository;

    private Feedback feedback1;
    private Feedback feedback2;
    private Long userId = 1L; // ID simulado de usuario para pruebas

    @BeforeEach
    public void setup() {
        feedbackRepository.deleteAll();

        // Crear y guardar feedbacks de prueba
        feedback1 = new Feedback();
        feedback1.setUserId(userId);
        feedback1.setGiftCardId(101L);
        feedback1.setMessage("Great gift card!");
        feedback1.setScore(5);

        feedback2 = new Feedback();
        feedback2.setUserId(userId);
        feedback2.setGiftCardId(102L);
        feedback2.setMessage("Good value for money.");
        feedback2.setScore(4);

        // Definir fechas de creación para pruebas de rango de fechas
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10); // Fecha de hace 10 días
        feedback1.setCreatedAt(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, +5); // Fecha de hace 5 días
        feedback2.setCreatedAt(cal.getTime());

        feedbackRepository.save(feedback1);
        feedbackRepository.save(feedback2);
    }

    @Test
    @DisplayName("Buscar feedbacks por ID de GiftCard")
    public void testFindByGiftCardId() {
        List<Feedback> feedbacks = feedbackRepository.findByGiftCardId(101L);
        assertEquals(1, feedbacks.size(), "Debería encontrar un feedback para el ID de GiftCard proporcionado");
        assertEquals("Great gift card!", feedbacks.get(0).getMessage(), "El mensaje debería coincidir");
    }

    @Test
    @DisplayName("Buscar feedbacks por ID de usuario")
    public void testFindByUserId() {
        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        assertEquals(2, feedbacks.size(), "Debería encontrar dos feedbacks para el ID de usuario proporcionado");
    }

    @Test
    @DisplayName("Buscar feedbacks creados después de una fecha específica")
    public void testFindByCreatedAtAfter() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7); // Fecha de hace 7 días
        Date date = cal.getTime();

        List<Feedback> feedbacks = feedbackRepository.findByCreatedAtAfter(date);
        assertEquals(1, feedbacks.size(), "Debería encontrar un feedback creado después de la fecha especificada");
        assertEquals("Good value for money.", feedbacks.get(0).getMessage(), "El mensaje debería coincidir");
    }

    @Test
    @DisplayName("Buscar feedbacks creados antes de una fecha específica")
    public void testFindByCreatedAtBefore() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7); // Fecha de hace 7 días
        Date date = cal.getTime();

        List<Feedback> feedbacks = feedbackRepository.findByCreatedAtBefore(date);
        assertEquals(1, feedbacks.size(), "Debería encontrar un feedback creado antes de la fecha especificada");
        assertEquals("Great gift card!", feedbacks.get(0).getMessage(), "El mensaje debería coincidir");
    }

    @Test
    @DisplayName("Buscar feedbacks creados dentro de un rango de fechas")
    public void testFindByCreatedAtBetween() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -12); // Fecha de hace 12 días
        Date startDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, +8); // Fecha de hace 4 días
        Date endDate = cal.getTime();

        List<Feedback> feedbacks = feedbackRepository.findByCreatedAtBetween(startDate, endDate);
        assertEquals(2, feedbacks.size(), "Debería encontrar dos feedbacks en el rango de fechas especificado");
    }

    @Test
    @DisplayName("Buscar feedbacks en un rango de fechas con @Query")
    public void testFindFeedbacksInDateRange() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -12); // Fecha de hace 12 días
        Date startDate = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, +8); // Fecha de hace 4 días
        Date endDate = cal.getTime();

        List<Feedback> feedbacks = feedbackRepository.findFeedbacksInDateRange(startDate, endDate);
        assertEquals(2, feedbacks.size(), "Debería encontrar dos feedbacks en el rango de fechas especificado usando el método @Query");
    }
}
