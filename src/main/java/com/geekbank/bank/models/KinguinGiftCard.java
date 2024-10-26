package com.geekbank.bank.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class KinguinGiftCard implements GiftCard {

    private boolean isHighlight;
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

    public KinguinGiftCard(String s, String title, double v, String date) {
        this.productId = s;
        this.name = title;
        this.price = v;
        this.releaseDate = date;
    }

    public KinguinGiftCard(){

    }

    public void setName(String name) {
        this.name = name;
    }
    public void setIsHighlight(Boolean value) {
        this.isHighlight = value;
    }

    public boolean getIsHighlight(Boolean value){
        return this.isHighlight;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setCoverImageOriginal(String coverImageOriginal) {
        this.coverImageOriginal = coverImageOriginal;
    }

    public void setDevelopers(List<String> developers) {
        this.developers = developers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setTextQty(int textQty) {
        this.textQty = textQty;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCheapestOfferId(List<String> cheapestOfferId) {
        this.cheapestOfferId = cheapestOfferId;
    }

    public void setPreorder(boolean isPreorder) {
        this.isPreorder = isPreorder;
    }

    public void setRegionalLimitations(String regionalLimitations) {
        this.regionalLimitations = regionalLimitations;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public void setActivationDetails(String activationDetails) {
        this.activationDetails = activationDetails;
    }

    public void setKinguinId(int kinguinId) {
        this.kinguinId = kinguinId;
    }
    public int getKinguinId(){ return this.kinguinId; }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots = screenshots;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public void setSystemRequirements(List<SystemRequirement> systemRequirements) {
        this.systemRequirements = systemRequirements;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public void setOffersCount(int offersCount) {
        this.offersCount = offersCount;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public void setMerchantName(List<String> merchantName) {
        this.merchantName = merchantName;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public KinguinGiftCard orElse(KinguinGiftCard other) {
        return this != null ? this : other;
    }

    public int getQty() {

        return this.qty;
    }

    public String getProductId() {
        return this.productId;
    }

    public String getDescription() {
        return this.description;
    }

    public String getPlatform() {
        return this.platform;
    }

    public int getTextQty() {
        return this.textQty;
    }

    public String getRegionalLimitations() {
        return this.regionalLimitations;
    }

    public int getRegionId() {
        return this.regionId;
    }

    public String getActivationDetails() {
        return this.activationDetails;
    }

    public String getOriginalName() {
        return this.originalName;
    }

    public int getOffersCount() {
        return this.offersCount;
    }

    public int getTotalQty() {
        return this.totalQty;
    }

    public String getAgeRating() {
        return this.ageRating;
    }

    public void setExpirationDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }


    // Getters and Setters...

    public static class Screenshot {
        @JsonProperty("url")
        private String url;

        @JsonProperty("urlOriginal")
        private String urlOriginal;

        public Screenshot() {
        }

        // Getters and Setters...

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUrlOriginal() {
            return urlOriginal;
        }

        public void setUrlOriginal(String urlOriginal) {
            this.urlOriginal = urlOriginal;
        }
    }

    public static class Video {
        @JsonProperty("name")
        private String name;

        @JsonProperty("videoId")
        private String videoId;

        public Video() {
        }

        // Getters and Setters...

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }
    }

    public static class SystemRequirement {
        @JsonProperty("system")
        private String system;

        @JsonProperty("requirement")
        private List<String> requirement;

        public SystemRequirement() {
        }

        // Getters and Setters...

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public List<String> getRequirement() {
            return requirement;
        }

        public void setRequirement(List<String> requirement) {
            this.requirement = requirement;
        }
    }

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

        public Offer() {
        }

        // Getters and Setters...

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOfferId() {
            return offerId;
        }

        public void setOfferId(String offerId) {
            this.offerId = offerId;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public int getTextQty() {
            return textQty;
        }

        public void setTextQty(int textQty) {
            this.textQty = textQty;
        }

        public int getAvailableQty() {
            return availableQty;
        }

        public void setAvailableQty(int availableQty) {
            this.availableQty = availableQty;
        }

        public int getAvailableTextQty() {
            return availableTextQty;
        }

        public void setAvailableTextQty(int availableTextQty) {
            this.availableTextQty = availableTextQty;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public boolean isPreorder() {
            return isPreorder;
        }

        public void setPreorder(boolean isPreorder) {
            this.isPreorder = isPreorder;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public int getQty() {
            return this.qty;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Images {
        @JsonProperty("screenshots")
        private List<Screenshot> screenshots;

        @JsonProperty("cover")
        private Cover cover;

        public Images() {
        }

        public static class Cover {
            @JsonProperty("thumbnail")
            private String thumbnail;

            public Cover() {
            }

            // Getters and Setters...

            public String getThumbnail() {
                return thumbnail;
            }

            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }
        }

        // Getters and Setters...

        public List<Screenshot> getScreenshots() {
            return screenshots;
        }

        public void setScreenshots(List<Screenshot> screenshots) {
            this.screenshots = screenshots;
        }

        public Cover getCover() {
            return cover;
        }

        public void setCover(Cover cover) {
            this.cover = cover;
        }
    }

    // Implementación de la interfaz GiftCard

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
        // Implementación de redención
    }

    @Override
    public String toString() {
        return "KinguinGiftCard{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", coverImage='" + coverImage + '\'' +
                ", coverImageOriginal='" + coverImageOriginal + '\'' +
                ", developers=" + developers +
                ", publishers=" + publishers +
                ", genres=" + genres +
                ", platform='" + platform + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", qty=" + qty +
                ", textQty=" + textQty +
                ", price=" + price +
                ", cheapestOfferId=" + cheapestOfferId +
                ", isPreorder=" + isPreorder +
                ", regionalLimitations='" + regionalLimitations + '\'' +
                ", regionId=" + regionId +
                ", activationDetails='" + activationDetails + '\'' +
                ", kinguinId=" + kinguinId +
                ", productId='" + productId + '\'' +
                ", originalName='" + originalName + '\'' +
                ", screenshots=" + screenshots +
                ", videos=" + videos +
                ", languages=" + languages +
                ", systemRequirements=" + systemRequirements +
                ", tags=" + tags +
                ", offers=" + offers +
                ", offersCount=" + offersCount +
                ", totalQty=" + totalQty +
                ", merchantName=" + merchantName +
                ", ageRating='" + ageRating + '\'' +
                ", images=" + images +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }


    // Getters y setters para todos los campos
    //...
}
