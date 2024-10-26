package com.geekbank.bank.services;

import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.FeedBackRepository;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RecommendationServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedBackRepository feedbackRepository;

    @Autowired
    private GiftCardRepository giftCardRepository;

    private User testUser;
    private GiftCardEntity giftCard1;
    private GiftCardEntity giftCard2;
    private GiftCardEntity giftCard3;

    @BeforeEach
    public void setup() {
        feedbackRepository.deleteAll();
        giftCardRepository.deleteAll();
        userRepository.deleteAll();

        // Crea un usuario de prueba
        testUser = new User();
        testUser.setName("Test User");
        testUser = userRepository.save(testUser);

        // Crea varias GiftCards de prueba
        giftCard1 = new GiftCardEntity();
        giftCard1.setKinguinId(1L);
        giftCard1.setProductId("prod1");
        giftCard1 = giftCardRepository.save(giftCard1);

        giftCard2 = new GiftCardEntity();
        giftCard2.setKinguinId(2L);
        giftCard2.setProductId("prod2");
        giftCard2 = giftCardRepository.save(giftCard2);

        giftCard3 = new GiftCardEntity();
        giftCard3.setKinguinId(3L);
        giftCard3.setProductId("prod3");
        giftCard3 = giftCardRepository.save(giftCard3);

        // Crea feedbacks para las GiftCards
        Feedback feedback1 = new Feedback();
        feedback1.setUserId(testUser.getId());
        feedback1.setGiftCardId(giftCard1.getKinguinId());
        feedback1.setScore(4);
        feedbackRepository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setUserId(testUser.getId());
        feedback2.setGiftCardId(giftCard2.getKinguinId());
        feedback2.setScore(3);
        feedbackRepository.save(feedback2);
    }

    @Test
    @DisplayName("Generar recomendaciones utilizando Slope One")
    public void testRecommendBySlopeOne() {
        List<KinguinGiftCard> recommendations = recommendationService.recommendBySlopeOne(testUser.getId(), 2);

        assertFalse(recommendations.isEmpty(), "Debería haber recomendaciones basadas en Slope One");
        assertEquals(1, recommendations.size(), "Debería haber exactamente una recomendación");
        assertEquals("prod3", recommendations.get(0).getProductId(), "La recomendación debería ser para giftCard3");
    }

    @Test
    @DisplayName("Generar recomendaciones por popularidad")
    public void testRecommendByPopularity() {
        List<KinguinGiftCard> recommendations = recommendationService.recommendByPopularity(2);

        assertFalse(recommendations.isEmpty(), "Debería haber recomendaciones basadas en popularidad");
        assertEquals(2, recommendations.size(), "Debería haber exactamente dos recomendaciones");
    }

    @Test
    @DisplayName("Generar recomendaciones para usuario sin feedbacks")
    public void testRecommendForUserWithoutFeedbacks() {
        // Crea un nuevo usuario sin feedback
        User newUser = new User();
        newUser.setName("New User");
        newUser = userRepository.save(newUser);

        List<KinguinGiftCard> recommendations = recommendationService.recommend(newUser.getId(), 2);

        assertFalse(recommendations.isEmpty(), "Debería haber recomendaciones alternativas basadas en popularidad");
    }

    @Test
    @DisplayName("Generar recomendaciones para usuario existente")
    public void testRecommendForExistingUser() {
        List<KinguinGiftCard> recommendations = recommendationService.recommend(testUser.getId(), 3);

        assertEquals(1, recommendations.size(), "Debería haber exactamente una recomendación por Slope One o popularidad");
    }
}
