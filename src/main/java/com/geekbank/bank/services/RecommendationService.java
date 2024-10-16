package com.geekbank.bank.services;

import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.repositories.FeedBackRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// Asegúrate de importar otros paquetes necesarios

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedBackRepository feedbackRepository;

    @Autowired
    private GiftCardRepository giftCardRepository;

    @Autowired
    private KinguinService giftCardService;

    private Set<String> existingGiftCardIds;

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @PostConstruct
    public void init() {
        // Obtener todos los GiftCards de la base de datos local
        List<GiftCardEntity> allGiftCards = giftCardRepository.findAll();
        existingGiftCardIds = allGiftCards.stream()
                .map(giftCard -> String.valueOf(giftCard.getKinguinId()))
                .collect(Collectors.toSet());

        logger.info("Total de GiftCards existentes en la base de datos: {}", existingGiftCardIds.size());
        logger.debug("Algunos GiftCards existentes: {}", existingGiftCardIds.stream().limit(10).collect(Collectors.toList()));
    }


    public List<KinguinGiftCard> recommendByUserSimilarity(Long userId, int k) {
        logger.info("Iniciando recomendaciones para el usuario ID: {}", userId);

        Optional<User> currentUserOpt = userRepository.findById(userId);
        if (currentUserOpt.isEmpty()) {
            logger.warn("Usuario con ID {} no encontrado.", userId);
            return Collections.emptyList();
        }
        User currentUser = currentUserOpt.get();
        logger.debug("Usuario actual: {}", currentUser.getName());

        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        logger.info("Total de feedbacks encontrados: {}", allFeedbacks.size());

        // Construir la matriz de puntuaciones de usuarios a gift cards
        Map<Long, Map<String, Integer>> userRatings = new HashMap<>();
        for (Feedback feedback : allFeedbacks) {
            try {
                Long uid = Long.valueOf(feedback.getUserId()); // Convertir String a Long
                String gid = feedback.getGiftCardId();
                int score = feedback.getScore();

                // Filtrar GiftCard IDs que no existen en la base de datos local
                if (!existingGiftCardIds.contains(gid)) {
                    logger.warn("GiftCard ID {} no existe. Se omitirá en las recomendaciones.", gid);
                    continue; // Omitir este feedback
                }

                userRatings.computeIfAbsent(uid, k1 -> new HashMap<>()).put(gid, score);
            } catch (NumberFormatException e) {
                logger.error("Error al convertir userId '{}' a Long.", feedback.getUserId(), e);
            }
        }

        logger.debug("Matriz de puntuaciones construida: {}", userRatings);

        // Obtener las puntuaciones del usuario actual
        Map<String, Integer> currentUserRatings = userRatings.get(currentUser.getId());
        if (currentUserRatings == null) {
            logger.warn("Usuario con ID {} no tiene feedbacks válidos.", userId);
            return Collections.emptyList();
        }
        logger.debug("Feedbacks del usuario actual: {}", currentUserRatings);

        // Calcular similaridad entre usuarios
        Map<Long, Double> similarityScores = new HashMap<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : userRatings.entrySet()) {
            Long otherUserId = entry.getKey();
            if (!otherUserId.equals(currentUser.getId())) {
                Map<String, Integer> otherUserRatings = entry.getValue();
                double similarity = cosineSimilarity(currentUserRatings, otherUserRatings);
                logger.debug("Similaridad entre usuario {} y usuario {}: {}", userId, otherUserId, similarity);
                similarityScores.put(otherUserId, similarity);
            }
        }

        // Ordenar usuarios por similaridad
        List<Long> similarUsers = similarityScores.entrySet().stream()
                .filter(e -> e.getValue() > 0) // Filtrar usuarios con similaridad > 0
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        logger.info("Usuarios similares: {}", similarUsers);

        if (similarUsers.isEmpty()) {
            logger.info("No se encontraron usuarios similares. No se generarán recomendaciones.");
            return Collections.emptyList();
        }

        // Recomendar gift cards que los usuarios similares han puntuado alto y el usuario actual no ha puntuado
        Map<String, Double> recommendationScores = new HashMap<>();
        for (Long similarUserId : similarUsers) {
            Map<String, Integer> similarUserRatings = userRatings.get(similarUserId);
            double similarity = similarityScores.get(similarUserId);

            for (Map.Entry<String, Integer> entry : similarUserRatings.entrySet()) {
                String giftCardId = entry.getKey();
                int rating = entry.getValue();
                if (!currentUserRatings.containsKey(giftCardId)) {
                    // Puedes ajustar el cálculo del puntaje de recomendación según tus necesidades
                    recommendationScores.merge(giftCardId, rating * similarity, Double::sum);
                }
            }
        }
        logger.debug("Puntajes de recomendaciones: {}", recommendationScores.keySet());

        // Ordenar las gift cards por el puntaje de recomendación
        List<String> recommendedGiftCardIds = recommendationScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        logger.info("GiftCards recomendadas IDs: {}", recommendedGiftCardIds);
        if (recommendedGiftCardIds.isEmpty()) {
            logger.info("No hay gift cards recomendadas basadas en similaridad.");
            return Collections.emptyList();
        }

        // Obtener los detalles de las gift cards recomendadas desde la base de datos local
        List<KinguinGiftCard> recommendedGiftCards = new ArrayList<>();
        for (String giftCardId : recommendedGiftCardIds) {
            try {
                Long kinguinId = Long.parseLong(giftCardId);
                Optional<GiftCardEntity> optionalGiftCard = giftCardRepository.findById(kinguinId);
                if (optionalGiftCard.isPresent()) {
                    GiftCardEntity entity = optionalGiftCard.get();
                    KinguinGiftCard kGiftCard = convertToKinguinGiftCard(entity);
                    recommendedGiftCards.add(kGiftCard);
                    logger.debug("GiftCard obtenida: {}", kGiftCard.getKinguinId());
                } else {
                    logger.warn("GiftCard con ID {} no encontrada en la base de datos local.", giftCardId);
                }
            } catch (NumberFormatException e) {
                logger.error("Error al convertir giftCardId '{}' a Long.", giftCardId, e);
            }
        }

        logger.info("Total de GiftCards recomendadas: {}", recommendedGiftCards.size());
        return recommendedGiftCards;
    }

    /**
     * Convierte un GiftCardEntity a KinguinGiftCard.
     */
    private KinguinGiftCard convertToKinguinGiftCard(GiftCardEntity entity) {
        KinguinGiftCard kGiftCard = new KinguinGiftCard();
        kGiftCard.setKinguinId(entity.getKinguinId().intValue());
        kGiftCard.setProductId(entity.getProductId());
        kGiftCard.setDescription(entity.getDescription());
        kGiftCard.setPrice(entity.getPrice());
        kGiftCard.setExpirationDate(entity.getReleaseDate());
        kGiftCard.setPlatform(entity.getPlatform());
        kGiftCard.setQty(entity.getQty());
        kGiftCard.setTextQty(entity.getTextQty());
        kGiftCard.setRegionalLimitations(entity.getRegionalLimitations());
        kGiftCard.setRegionId(entity.getRegionId());
        kGiftCard.setActivationDetails(entity.getActivationDetails());
        kGiftCard.setOriginalName(entity.getOriginalName());
        kGiftCard.setOffersCount(entity.getOffersCount());
        kGiftCard.setTotalQty(entity.getTotalQty());
        kGiftCard.setAgeRating(entity.getAgeRating());
        // Asigna otros campos según sea necesario

        return kGiftCard;
    }

    /**
     * Calcula la similaridad coseno entre dos usuarios.
     */
    private double cosineSimilarity(Map<String, Integer> ratings1, Map<String, Integer> ratings2) {
        Set<String> commonItems = new HashSet<>(ratings1.keySet());
        commonItems.retainAll(ratings2.keySet());

        if (commonItems.isEmpty()) {
            logger.debug("No hay items comunes entre los usuarios.");
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String item : commonItems) {
            int rating1 = ratings1.get(item);
            int rating2 = ratings2.get(item);
            dotProduct += rating1 * rating2;
        }

        for (int rating : ratings1.values()) {
            norm1 += rating * rating;
        }
        norm1 = Math.sqrt(norm1);

        for (int rating : ratings2.values()) {
            norm2 += rating * rating;
        }
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0.0 || norm2 == 0.0) {
            logger.debug("Normas de los vectores de ratings son cero.");
            return 0.0;
        }

        double similarity = dotProduct / (norm1 * norm2);
        logger.debug("Similaridad calculada: {}", similarity);
        return similarity;
    }
}
