package com.geekbank.bank.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.FeedBackRepository;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.geekbank.bank.services.KinguinService.apiKey;

@Service
public class RecommendationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedBackRepository feedbackRepository;
    private static final String apiUrl = "https://gateway.kinguin.net/esa/api/v1/products";

    @Autowired
    private GiftCardRepository giftCardRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    private Set<Long> existingGiftCardIds;

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    /**
     * Inicializa el conjunto de IDs de GiftCards existentes en la base de datos local.
     */
    @PostConstruct
    public void init() {
        List<GiftCardEntity> allGiftCards = giftCardRepository.findAll();
        existingGiftCardIds = allGiftCards.stream()
                .map(GiftCardEntity::getKinguinId)
                .collect(Collectors.toSet());

        logger.info("Total de GiftCards existentes en la base de datos: {}", existingGiftCardIds.size());
        logger.debug("Algunos GiftCards existentes: {}", existingGiftCardIds.stream().limit(10).collect(Collectors.toList()));
    }

    /**
     * Genera recomendaciones para un usuario.
     * Primero intenta basarse en el algoritmo Slope One.
     * Si no se encuentran recomendaciones, recurre a recomendaciones por popularidad.
     *
     * @param userId ID del usuario para el cual se generan las recomendaciones.
     * @param k      Número máximo de recomendaciones a generar.
     * @return Lista de GiftCards recomendadas.
     */
    public List<KinguinGiftCard> recommend(Long userId, int k) {
        List<KinguinGiftCard> recommendations = recommendBySlopeOne(userId, k);

        if (recommendations.isEmpty()) {
            logger.info("No se encontraron recomendaciones por similaridad. Recurriendo a recomendaciones por popularidad.");
            recommendations = recommendByPopularity(k);
        }

        return recommendations;
    }

    /**
     * Genera recomendaciones basadas en el algoritmo Slope One.
     *
     * @param userId ID del usuario para el cual se generan las recomendaciones.
     * @param k      Número máximo de recomendaciones a generar.
     * @return Lista de GiftCards recomendadas basadas en Slope One.
     */
    public List<KinguinGiftCard> recommendBySlopeOne(Long userId, int k) {
        logger.info("Iniciando recomendaciones por similaridad (Slope One) para el usuario ID: {}", userId);

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
        Map<Long, Map<Long, Double>> userRatings = new HashMap<>();
        for (Feedback feedback : allFeedbacks) {
            try {
                Long uid = feedback.getUserId();
                Long gid = feedback.getGiftCardId();
                double score = feedback.getScore();

                // Filtrar GiftCard IDs que no existen en la base de datos local
                if (!existingGiftCardIds.contains(gid)) {
                    logger.warn("GiftCard ID {} no existe. Se omitirá en las recomendaciones.", gid);
                    continue; // Omitir este feedback
                }

                userRatings.computeIfAbsent(uid, k1 -> new HashMap<>()).put(gid, score);
            } catch (Exception e) {
                logger.error("Error al procesar feedback: {}", feedback, e);
            }
        }

        logger.debug("Matriz de puntuaciones construida: {}", userRatings);
        logger.info("Total de usuarios con feedbacks válidos: {}", userRatings.size());

        // Obtener las puntuaciones del usuario actual
        Map<Long, Double> currentUserRatings = userRatings.get(currentUser.getId());
        if (currentUserRatings == null || currentUserRatings.isEmpty()) {
            logger.warn("Usuario con ID {} no tiene feedbacks válidos.", userId);
            return Collections.emptyList();
        }
        logger.debug("Feedbacks del usuario actual: {}", currentUserRatings);

        // Calcular las diferencias y frecuencias entre items (Slope One)
        Map<Long, Map<Long, Double>> diff = new HashMap<>();
        Map<Long, Map<Long, Integer>> freq = new HashMap<>();

        for (Map.Entry<Long, Map<Long, Double>> userEntry : userRatings.entrySet()) {
            Long uid = userEntry.getKey();
            Map<Long, Double> ratings = userEntry.getValue();

            for (Map.Entry<Long, Double> e1 : ratings.entrySet()) {
                Long item1 = e1.getKey();
                Double rating1 = e1.getValue();

                for (Map.Entry<Long, Double> e2 : ratings.entrySet()) {
                    Long item2 = e2.getKey();
                    Double rating2 = e2.getValue();

                    if (!item1.equals(item2)) {
                        // Initialize maps if not present
                        diff.putIfAbsent(item1, new HashMap<>());
                        freq.putIfAbsent(item1, new HashMap<>());

                        // Update difference and frequency
                        double observedDiff = rating1 - rating2;
                        diff.get(item1).put(item2, diff.get(item1).getOrDefault(item2, 0.0) + observedDiff);
                        freq.get(item1).put(item2, freq.get(item1).getOrDefault(item2, 0) + 1);
                    }
                }
            }
        }

        // Calcular diferencias promedio
        Map<Long, Map<Long, Double>> avgDiff = new HashMap<>();
        for (Long item1 : diff.keySet()) {
            avgDiff.putIfAbsent(item1, new HashMap<>());
            for (Long item2 : diff.get(item1).keySet()) {
                double totalDiff = diff.get(item1).get(item2);
                int count = freq.get(item1).get(item2);
                avgDiff.get(item1).put(item2, totalDiff / count);
            }
        }

        logger.debug("Diferencias promedio calculadas: {}", avgDiff);

        // Predecir puntuaciones para items no evaluados
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> predictionCounts = new HashMap<>();

        for (Map.Entry<Long, Double> entry : currentUserRatings.entrySet()) {
            Long item = entry.getKey();
            Double rating = entry.getValue();

            if (avgDiff.containsKey(item)) {
                for (Map.Entry<Long, Double> diffEntry : avgDiff.get(item).entrySet()) {
                    Long otherItem = diffEntry.getKey();
                    Double avgDifference = diffEntry.getValue();

                    // Predicted rating for otherItem
                    Double predictedRating = rating + avgDifference;

                    // Accumulate predictions
                    predictions.put(otherItem, predictions.getOrDefault(otherItem, 0.0) + predictedRating);
                    predictionCounts.put(otherItem, predictionCounts.getOrDefault(otherItem, 0) + 1);
                    logger.debug("Predicción acumulada para GiftCard ID {}: {}", otherItem, predictedRating);
                }
            }
        }

        // Calcular puntuaciones finales
        Map<Long, Double> finalPredictions = new HashMap<>();
        for (Long item : predictions.keySet()) {
            if (!currentUserRatings.containsKey(item)) { // Excluir items ya evaluados
                double predictedScore = predictions.get(item) / predictionCounts.get(item);
                finalPredictions.put(item, predictedScore);
                logger.debug("Predicción final para GiftCard ID {}: {}", item, predictedScore);
            }
        }

        // Ordenar las predicciones y obtener los top K
        List<Long> recommendedGiftCardIds = finalPredictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info("GiftCards recomendadas IDs por similaridad (Slope One): {}", recommendedGiftCardIds);

        if (recommendedGiftCardIds.isEmpty()) {
            logger.info("No hay gift cards recomendadas basadas en similaridad (Slope One).");
            return Collections.emptyList();
        }

        // Obtener los detalles de las gift cards recomendadas desde la base de datos local
        List<KinguinGiftCard> recommendedGiftCards = new ArrayList<>();
        for (Long giftCardId : recommendedGiftCardIds) {
            try {
                Optional<GiftCardEntity> optionalGiftCard = giftCardRepository.findById(giftCardId);
                String giftCardIdStr = String.valueOf(giftCardId);
                KinguinGiftCard kGiftCard = fetchGiftCardById(giftCardIdStr);


                if (optionalGiftCard.isPresent()) {
                    GiftCardEntity entity = optionalGiftCard.get();
                    recommendedGiftCards.add(kGiftCard);
                    logger.debug("GiftCard obtenida: {}", kGiftCard.getKinguinId());
                } else {
                    logger.warn("GiftCard con ID {} no encontrada en la base de datos local.", giftCardId);
                }
            } catch (Exception e) {
                logger.error("Error al procesar giftCardId '{}': {}", giftCardId, e.getMessage());
            }
        }

        logger.info("Total de GiftCards recomendadas por similaridad (Slope One): {}", recommendedGiftCards.size());
        return recommendedGiftCards;
    }

    /**
     * Genera recomendaciones basadas en la popularidad de los GiftCards.
     *
     * @param k Número máximo de recomendaciones a generar.
     * @return Lista de GiftCards recomendadas basadas en popularidad.
     */
    public List<KinguinGiftCard> recommendByPopularity(int k) {
        Pageable pageable = PageRequest.of(0, k);
        List<GiftCardEntity> popularGiftCards = giftCardRepository.findTopKPopular(pageable);

        // Extraer los IDs de las GiftCards populares
        List<Long> popularGiftCardIds = popularGiftCards.stream()
                .map(GiftCardEntity::getKinguinId)
                .collect(Collectors.toList());

        logger.info("GiftCards recomendadas por popularidad IDs: {}", popularGiftCardIds);

        // Obtener los detalles de las GiftCards desde la API externa
        List<KinguinGiftCard> fetchedGiftCards = popularGiftCardIds.stream()
                .map(giftCardId -> {
                    String giftCardIdStr = String.valueOf(giftCardId);
                    KinguinGiftCard kGiftCard = fetchGiftCardById(giftCardIdStr);
                    if (kGiftCard != null) {
                        logger.debug("GiftCard obtenida desde API externa: {}", kGiftCard.getKinguinId());
                    } else {
                        logger.warn("GiftCard con ID {} no encontrada en el API externo.", giftCardIdStr);
                    }
                    return kGiftCard;
                })
                .filter(Objects::nonNull) // Filtrar las GiftCards que no fueron encontradas
                .collect(Collectors.toList());

        logger.info("Total de GiftCards recomendadas por popularidad: {}", fetchedGiftCards.size());

        return fetchedGiftCards;
    }

    public KinguinGiftCard fetchGiftCardById(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl + "/" + id, HttpMethod.GET, entity, JsonNode.class);
        JsonNode product = response.getBody();

        if (product != null) {
            return mapJsonToGiftCard(product);
        }

        return null;
    }

    private KinguinGiftCard mapJsonToGiftCard(JsonNode product) {
        KinguinGiftCard giftCard = new KinguinGiftCard();
        giftCard.setName(product.path("name").asText());
        giftCard.setDescription(product.path("description").asText());
        giftCard.setCoverImage(product.path("coverImage").asText());
        giftCard.setCoverImageOriginal(product.path("coverImageOriginal").asText());
        System.out.println(product.path("CoverImageOriginal"));
        giftCard.setDevelopers(convertJsonNodeToList(product.path("developers")));
        giftCard.setPublishers(convertJsonNodeToList(product.path("publishers")));
        giftCard.setGenres(convertJsonNodeToList(product.path("genres")));
        giftCard.setPlatform(product.path("platform").asText());
        giftCard.setReleaseDate(product.path("releaseDate").asText());
        giftCard.setQty(product.path("qty").asInt());
        giftCard.setTextQty(product.path("textQty").asInt());
        giftCard.setPrice(product.path("price").asDouble());
        giftCard.setCheapestOfferId(convertJsonNodeToList(product.path("cheapestOfferId")));
        giftCard.setPreorder(product.path("isPreorder").asBoolean());
        giftCard.setRegionalLimitations(product.path("regionalLimitations").asText());
        giftCard.setRegionId(product.path("regionId").asInt());
        giftCard.setActivationDetails(product.path("activationDetails").asText());
        giftCard.setKinguinId(product.path("kinguinId").asInt());
        giftCard.setProductId(product.path("productId").asText());
        giftCard.setOriginalName(product.path("originalName").asText());
        giftCard.setScreenshots(convertJsonNodeToListOfScreenshots(product.path("screenshots")));
        giftCard.setVideos(convertJsonNodeToListOfVideos(product.path("videos")));
        giftCard.setLanguages(convertJsonNodeToList(product.path("languages")));
        giftCard.setSystemRequirements(convertJsonNodeToListOfSystemRequirements(product.path("systemRequirements")));
        giftCard.setTags(convertJsonNodeToList(product.path("tags")));
        giftCard.setOffers(convertJsonNodeToListOfOffers(product.path("offers")));
        giftCard.setOffersCount(product.path("offersCount").asInt());
        giftCard.setTotalQty(product.path("totalQty").asInt());
        giftCard.setMerchantName(convertJsonNodeToList(product.path("merchantName")));
        giftCard.setAgeRating(product.path("ageRating").asText());
        giftCard.setImages(convertJsonNodeToImages(product.path("images")));
        System.out.println(product.path("images"));

        return giftCard;
    }

    private List<KinguinGiftCard.Screenshot> convertJsonNodeToListOfScreenshots(JsonNode node) {
        List<KinguinGiftCard.Screenshot> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                KinguinGiftCard.Screenshot screenshot = new KinguinGiftCard.Screenshot();
                screenshot.setUrl(jsonNode.path("url").asText());
                screenshot.setUrlOriginal(jsonNode.path("url_original").asText());
                list.add(screenshot);
            }
        }
        return list;
    }

    private List<KinguinGiftCard.Video> convertJsonNodeToListOfVideos(JsonNode node) {
        List<KinguinGiftCard.Video> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                KinguinGiftCard.Video video = new KinguinGiftCard.Video();
                video.setName(jsonNode.path("name").asText());
                video.setVideoId(jsonNode.path("video_id").asText());
                list.add(video);
            }
        }
        return list;
    }

    private List<KinguinGiftCard.SystemRequirement> convertJsonNodeToListOfSystemRequirements(JsonNode node) {
        List<KinguinGiftCard.SystemRequirement> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                KinguinGiftCard.SystemRequirement systemRequirement = new KinguinGiftCard.SystemRequirement();
                systemRequirement.setSystem(jsonNode.path("system").asText());
                systemRequirement.setRequirement(convertJsonNodeToList(jsonNode.path("requirement")));
                list.add(systemRequirement);
            }
        }
        return list;
    }

    private List<KinguinGiftCard.Offer> convertJsonNodeToListOfOffers(JsonNode node) {
        List<KinguinGiftCard.Offer> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                KinguinGiftCard.Offer offer = new KinguinGiftCard.Offer();
                offer.setName(jsonNode.path("name").asText());
                offer.setOfferId(jsonNode.path("offerId").asText());
                offer.setPrice(jsonNode.path("price").asDouble());
                offer.setQty(jsonNode.path("qty").asInt());
                offer.setTextQty(jsonNode.path("textQty").asInt());
                offer.setAvailableQty(jsonNode.path("availableQty").asInt());
                offer.setAvailableTextQty(jsonNode.path("availableTextQty").asInt());
                offer.setMerchantName(jsonNode.path("merchantName").asText());
                offer.setPreorder(jsonNode.path("isPreorder").asBoolean());
                offer.setReleaseDate(jsonNode.path("releaseDate").asText());
                list.add(offer);
            }
        }
        return list;
    }

    private KinguinGiftCard.Images convertJsonNodeToImages(JsonNode node) {
        KinguinGiftCard.Images images = new KinguinGiftCard.Images();
        if (node.has("screenshots")) {
            images.setScreenshots(convertJsonNodeToListOfScreenshots(node.path("screenshots")));
        }
        if (node.has("cover")) {
            KinguinGiftCard.Images.Cover cover = new KinguinGiftCard.Images.Cover();
            cover.setThumbnail(node.path("cover").path("thumbnail").asText());
            images.setCover(cover);
        }
        return images;
    }

    private List<String> convertJsonNodeToList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                list.add(jsonNode.asText());
            }
        }
        return list;
    }


    /**
     * Convierte un GiftCardEntity a KinguinGiftCard.
     *
     * @param entity Entidad de GiftCard desde la base de datos.
     * @return Objeto KinguinGiftCard listo para ser enviado al cliente.
     */
    private KinguinGiftCard convertToKinguinGiftCard(GiftCardEntity entity) {
        KinguinGiftCard kGiftCard = new KinguinGiftCard();
        kGiftCard.setKinguinId(entity.getKinguinId().intValue());
        kGiftCard.setProductId(entity.getProductId());
        kGiftCard.setDescription(entity.getDescription());
        kGiftCard.setPrice(entity.getPrice());

        kGiftCard.setPlatform(entity.getPlatform());
        kGiftCard.setQty(entity.getQty());

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
}
