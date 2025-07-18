package com.geekbank.bank.giftcard.kinguin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.giftcard.mapper.GiftCardMapper;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.model.GiftCardEntity;
import com.geekbank.bank.giftcard.repository.GiftCardRepository;
import com.geekbank.bank.support.currency.PricingService;
import com.geekbank.bank.user.service.UserService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.deepl.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class KinguinService {

    public static final String apiUrl = "https://gateway.kinguin.net/esa/api/v1/products";
    public static final String apiKey = "77d96c852356b1c654a80f424d67048f";
    Translator translator;
    protected static String deeplApiKey = "b9809fe3-aa7c-4802-b12b-cacef4df6e2a:fx";
    private static AtomicInteger progress = new AtomicInteger(0);
    private static boolean isSyncing = false;
    private static int totalGiftCards = 0;
    private static int synchronizedGiftCards = 0;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PricingService pricingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GiftCardRepository giftCardRepository;


    public List<KinguinGiftCard> applyGlobalMargin(List<KinguinGiftCard> giftCards) {
        for (KinguinGiftCard card : giftCards) {
            double finalPrice = pricingService.calculateSellingPrice(card.getPrice());
            card.setPrice(finalPrice);
        }
        return giftCards;
    }

    public List<KinguinGiftCard> fetchAllGiftCards() {
        List<KinguinGiftCard> allGiftCards = new ArrayList<>();
        int currentPage = 1;
        boolean hasMore = true;

        while (hasMore) {
            List<KinguinGiftCard> giftCards = fetchGiftCards(currentPage);
            if (giftCards.isEmpty()) {
                hasMore = false;
            } else {
                for (KinguinGiftCard kCard : giftCards) {
                    Long kinguinId = (long) kCard.getKinguinId();

                    try {
                        if (giftCardRepository.existsById(kinguinId)) {

                            GiftCardEntity existingEntity = giftCardRepository.findById(kinguinId).orElse(null);
                            if (existingEntity != null) {

                                GiftCardMapper.updateGiftCardEntity(existingEntity, kCard);
                                giftCardRepository.save(existingEntity);
                                logger.info("GiftCard ID {} actualizada exitosamente.", existingEntity.getKinguinId());
                            }
                        } else {

                            GiftCardEntity newEntity = GiftCardMapper.mapToGiftCardEntity(kCard);
                            giftCardRepository.save(newEntity);
                            logger.info("GiftCard ID {} insertada exitosamente.", newEntity.getKinguinId());
                        }

                        synchronizedGiftCards++;
                        progress.set((int) ((synchronizedGiftCards / (double) totalGiftCards) * 100));

                    } catch (Exception e) {
                        logger.error("Error al procesar GiftCard ID {}: {}", kinguinId, e.getMessage());
                    }

                    allGiftCards.add(kCard);
                }

                currentPage++;
                logger.debug("Página {}: {} GiftCards obtenidas.", currentPage, giftCards.size());


                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupción durante el sleep entre solicitudes de páginas: {}", e.getMessage());
                    break;
                }
            }
        }

        logger.info("Total de GiftCards obtenidas: {}", allGiftCards.size());
        return allGiftCards;
    }



    private String truncate(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        return value;
    }

    public static int getProgress() {
        return progress.get();
    }

    public static boolean isSyncing() {
        return isSyncing;
    }

    public static int getTotalGiftCards() {
        return totalGiftCards;
    }

    public static int getSynchronizedGiftCards() {
        return synchronizedGiftCards;
    }


    public List<KinguinGiftCard> fetchGiftCards(int page) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl + "?page=" + page, HttpMethod.GET, entity, JsonNode.class);
        JsonNode products = response.getBody();

        List<KinguinGiftCard> giftCards = new ArrayList<>();
        if (products != null) {
            JsonNode productsSection = products.path("results");
            for (JsonNode product : productsSection) {
                giftCards.add(mapJsonToGiftCard(product));
            }
        }

        giftCards.removeIf(
                giftcard -> giftcard.getRegionId() != 3
        );

//        giftCards.removeIf(
//                giftcard -> giftcard.getRegionId() != 3
//                        && giftcard.getRegionId() != 4
//                        && giftcard.getRegionId() != 11
//
//        );
        return giftCards;
    }


    public Map<Long, KinguinGiftCard> fetchGiftCardsByIds(List<Long> productIds) {
        return productIds.parallelStream()
                .map(productId -> {
                    KinguinGiftCard giftCard = fetchGiftCardById(String.valueOf(productId)).orElse(null);
                    return new AbstractMap.SimpleEntry<>(productId, giftCard);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<KinguinGiftCard> searchGiftCardsByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String searchUrl = apiUrl + "?name=" + name + "&limit=500";
        ResponseEntity<JsonNode> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, JsonNode.class);
        JsonNode products = response.getBody();

        List<KinguinGiftCard> giftCards = new ArrayList<>();
        if (products != null) {
            JsonNode productsSection = products.path("results");
            for (JsonNode product : productsSection) {
                giftCards.add(mapJsonToGiftCard(product));
            }
        }

        giftCards.sort(Comparator.comparingDouble(KinguinGiftCard::getPrice));
        giftCards.removeIf(
                giftcard -> giftcard.getRegionId() != 3
        );
//        giftCards.removeIf(
//                giftcard -> giftcard.getRegionId() != 3
//                        && giftcard.getRegionId() != 4
//                        && giftcard.getRegionId() != 11
//
//        );

        return giftCards;
    }

    public List<KinguinGiftCard> fetchFilteredGiftCards(Map<String, String> filters) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?");

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }

        String finalUrl = urlBuilder.toString();
        finalUrl = finalUrl.endsWith("&") ? finalUrl.substring(0, finalUrl.length() - 1) : finalUrl;
        System.out.println("Final URL: " + finalUrl);

        ResponseEntity<JsonNode> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, JsonNode.class);
        JsonNode products = response.getBody();

        List<KinguinGiftCard> giftCards = new ArrayList<>();
        if (products != null) {
            JsonNode productsSection = products.path("results");
            for (JsonNode product : productsSection) {
                    giftCards.add(mapJsonToGiftCard(product));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            giftCards.sort((g1, g2) -> {
                try {
                    String rd1 = g1.getReleaseDate();
                    String rd2 = g2.getReleaseDate();

                    if (rd1 == null || rd1.isEmpty()) {
                        return 1;
                    }
                    if (rd2 == null || rd2.isEmpty()) {
                        return -1;
                    }

                    LocalDateTime d1 = LocalDateTime.parse(rd1, DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime d2 = LocalDateTime.parse(rd2, DateTimeFormatter.ISO_DATE_TIME);

                    return d2.compareTo(d1);
                } catch (DateTimeParseException e) {
                    System.err.println("Error al parsear releaseDate: " + e.getMessage());
                    return 0;
                }
            });
        }

        giftCards.removeIf(
                giftcard -> giftcard.getRegionId() != 3
        );

//            giftCards.removeIf(
//                    giftcard -> giftcard.getRegionId() != 3
//                    && giftcard.getRegionId() != 4
//                    && giftcard.getRegionId() != 11
//
//            );
            return giftCards;
        }


    @SneakyThrows
    @Cacheable("giftCards")
    public KinguinGiftCard fetchGiftCardById(String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl + "/" + id, HttpMethod.GET, entity, JsonNode.class);
        JsonNode product = response.getBody();

        if (product != null) {

            KinguinGiftCard giftcard = mapJsonToGiftCard(product);

            return giftcard;
        }

        return null;
    }


    public String translateText(String text) throws DeepLException, InterruptedException {
        translator = new Translator(deeplApiKey);
        TextResult result = translator.translateText(text, "EN", "ES");
        return result.getText();
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

    private List<String> convertJsonNodeToList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode jsonNode : node) {
                list.add(jsonNode.asText());
            }
        }
        return list;
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
}
