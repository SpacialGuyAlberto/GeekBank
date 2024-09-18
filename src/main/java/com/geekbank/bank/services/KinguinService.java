package com.geekbank.bank.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.geekbank.bank.models.GiftCard;
import com.geekbank.bank.models.KinguinGiftCard;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class KinguinService {

    private static final String apiUrl = "https://gateway.kinguin.net/esa/api/v1/products";
    private static final String apiKey = "77d96c852356b1c654a80f424d67048f";
    private final RestTemplate restTemplate = new RestTemplate();


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

        return giftCards;
    }

    public List<KinguinGiftCard> searchGiftCardsByName(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String searchUrl = apiUrl + "?name=" + name;
        ResponseEntity<JsonNode> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, JsonNode.class);
        JsonNode products = response.getBody();

        List<KinguinGiftCard> giftCards = new ArrayList<>();
        if (products != null) {
            JsonNode productsSection = products.path("results");
            for (JsonNode product : productsSection) {
                giftCards.add(mapJsonToGiftCard(product));
            }
        }

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
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        String finalUrl = urlBuilder.toString();
        // Remover el último "&"
        finalUrl = finalUrl.endsWith("&") ? finalUrl.substring(0, finalUrl.length() - 1) : finalUrl;
        System.out.print(" final url : " + finalUrl);

        ResponseEntity<JsonNode> response = restTemplate.exchange(finalUrl, HttpMethod.GET, entity, JsonNode.class);
        JsonNode products = response.getBody();

        List<KinguinGiftCard> giftCards = new ArrayList<>();
        if (products != null) {
            JsonNode productsSection = products.path("results");
            for (JsonNode product : productsSection) {
                giftCards.add(mapJsonToGiftCard(product));
            }
        }

        return giftCards;
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
