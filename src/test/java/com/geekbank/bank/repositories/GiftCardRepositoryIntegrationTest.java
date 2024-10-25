package com.geekbank.bank.repositories;

import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class GiftCardRepositoryIntegrationTest {

    @Autowired
    private GiftCardRepository giftCardRepository;

    @Autowired
    private FeedBackRepository feedbackRepository;

    private GiftCardEntity giftCard1;
    private GiftCardEntity giftCard2;
    private GiftCardEntity giftCard3;

    @BeforeEach
    public void setup() {
        feedbackRepository.deleteAll();
        giftCardRepository.deleteAll();

        // Crear y guardar tarjetas de regalo
        giftCard1 = new GiftCardEntity();
        giftCard1.setKinguinId(1001L);
        giftCard1.setName("Gift Card 1");

        giftCard2 = new GiftCardEntity();
        giftCard2.setKinguinId(1002L);
        giftCard2.setName("Gift Card 2");

        giftCard3 = new GiftCardEntity();
        giftCard3.setKinguinId(1003L);
        giftCard3.setName("Gift Card 3");

        giftCardRepository.save(giftCard1);
        giftCardRepository.save(giftCard2);
        giftCardRepository.save(giftCard3);

        // Crear y guardar feedbacks asociados a las gift cards
        feedbackRepository.save(createFeedback(giftCard1.getKinguinId()));
        feedbackRepository.save(createFeedback(giftCard1.getKinguinId()));
        feedbackRepository.save(createFeedback(giftCard2.getKinguinId()));
        feedbackRepository.save(createFeedback(giftCard2.getKinguinId()));
        feedbackRepository.save(createFeedback(giftCard2.getKinguinId()));
        feedbackRepository.save(createFeedback(giftCard3.getKinguinId()));
    }

    private Feedback createFeedback(Long giftCardId) {
        Feedback feedback = new Feedback();
        feedback.setGiftCardId(giftCardId);
        feedback.setScore(5);
        feedback.setMessage("Great product!");
        feedback.setCreatedAt(new java.util.Date());
        return feedback;
    }

    @Test
    @DisplayName("Buscar las gift cards más populares basadas en el número de feedbacks")
    public void testFindTopKPopular() {
        // Recuperar las dos tarjetas de regalo más populares
        List<GiftCardEntity> popularGiftCards = giftCardRepository.findTopKPopular(PageRequest.of(0, 2));

        assertNotNull(popularGiftCards, "La lista de gift cards populares no debería ser nula");
        assertEquals(2, popularGiftCards.size(), "Debería haber dos gift cards en la lista");

        // Verificar que las tarjetas se ordenen correctamente según el número de comentarios
        assertEquals(giftCard2.getKinguinId(), popularGiftCards.get(0).getKinguinId(), "La tarjeta más popular debería ser giftCard2");
        assertEquals(giftCard1.getKinguinId(), popularGiftCards.get(1).getKinguinId(), "La segunda tarjeta más popular debería ser giftCard1");
    }
}
