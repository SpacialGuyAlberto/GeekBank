package com.geekbank.bank.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KinguinGiftCard implements GiftCard {

    private static final double EXCHANGE_RATE_TO_HNL = 26.5;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("coverImage")
    private String coverImage;

    @JsonProperty("coverImageOriginal")
    private String coverImageOriginal;

    @JsonProperty("developers")
    private List<String> developers;

    @JsonProperty("publishers")
    private List<String> publishers;

    @JsonProperty("genres")
    private List<String> genres;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("releaseDate")
    private String releaseDate;

    @JsonProperty("qty")
    private int qty;

    @JsonProperty("textQty")
    private int textQty;

    @JsonProperty("price")
    private double price;

    @JsonProperty("priceHNL")
    private double priceHNL;

    @JsonProperty("cheapestOfferId")
    private List<String> cheapestOfferId;

    @JsonProperty("isPreorder")
    private boolean isPreorder;

    @JsonProperty("regionalLimitations")
    private String regionalLimitations;

    @JsonProperty("regionId")
    private int regionId;

    @JsonProperty("activationDetails")
    private String activationDetails;

    @JsonProperty("kinguinId")
    private int kinguinId;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("originalName")
    private String originalName;

    @JsonProperty("screenshots")
    private List<Screenshot> screenshots;

    @JsonProperty("videos")
    private List<Video> videos;

    @JsonProperty("languages")
    private List<String> languages;

    @JsonProperty("systemRequirements")
    private List<SystemRequirement> systemRequirements;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("offers")
    private List<Offer> offers;

    @JsonProperty("offersCount")
    private int offersCount;

    @JsonProperty("totalQty")
    private int totalQty;

    @JsonProperty("merchantName")
    private List<String> merchantName;

    @JsonProperty("ageRating")
    private String ageRating;

    @JsonProperty("images")
    private Images images;

    @JsonProperty("updatedAt")
    private String updatedAt;

    private boolean isHighlight;

    public KinguinGiftCard() {}

    public KinguinGiftCard(String s, String title, double v, String date) {
        this.productId = s;
        this.name = title;
        this.price = v;
        this.releaseDate = date;
    }

//    public void setPrice(double price) {
////        this.price = price + (price * 0.10);
////        this.priceHNL = Math.round(convertToLempiras(this.price));
//        this.price = price;
//    }

    public double getPriceHNL() {
        return this.priceHNL;
    }

    public boolean getIsHighlight(Boolean value) {
        return this.isHighlight;
    }

    public void setExpirationDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    private double convertToLempiras(double priceUSD) {
        return Math.round(priceUSD * EXCHANGE_RATE_TO_HNL);
    }

    public KinguinGiftCard orElse(KinguinGiftCard other) {
        return this != null ? this : other;
    }

    // Métodos de GiftCard
    @Override
    public String getCardNumber() {
        return productId;
    }

    @Override
    public String getProduct() {
        return name;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getExpirationDate() {
        return releaseDate;
    }

    @Override
    public void redeem() {
        // implementación de redención
    }

    // Clases internas
    @Getter
    @Setter
    @ToString
    public static class Screenshot {
        @JsonProperty("url")
        private String url;

        @JsonProperty("urlOriginal")
        private String urlOriginal;
    }

    @Getter
    @Setter
    @ToString
    public static class Video {
        @JsonProperty("name")
        private String name;

        @JsonProperty("videoId")
        private String videoId;
    }

    @Getter
    @Setter
    @ToString
    public static class SystemRequirement {
        @JsonProperty("system")
        private String system;

        @JsonProperty("requirement")
        private List<String> requirement;
    }

    @Getter
    @Setter
    @ToString
    public static class Offer {
        @JsonProperty("name")
        private String name;

        @JsonProperty("offerId")
        private String offerId;

        @JsonProperty("price")
        private double price;

        @JsonProperty("qty")
        private int qty;

        @JsonProperty("textQty")
        private int textQty;

        @JsonProperty("availableQty")
        private int availableQty;

        @JsonProperty("availableTextQty")
        private int availableTextQty;

        @JsonProperty("merchantName")
        private String merchantName;

        @JsonProperty("isPreorder")
        private boolean isPreorder;

        @JsonProperty("releaseDate")
        private String releaseDate;
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ToString
    public static class Images {
        @JsonProperty("screenshots")
        private List<Screenshot> screenshots;

        @JsonProperty("cover")
        private Cover cover;

        @Getter
        @Setter
        @ToString
        public static class Cover {
            @JsonProperty("thumbnail")
            private String thumbnail;
        }
    }
}
