package com.geekbank.bank.models;

import com.geekbank.bank.converters.ListToStringConverter;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "gift_cards")
public class GiftCardEntity {


    public Long getKinguinId() {
        return kinguinId;
    }

    public void setKinguinId(Long kinguinId) {
        this.kinguinId = kinguinId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getTextQty() {
        return textQty;
    }

    public void setTextQty(Integer textQty) {
        this.textQty = textQty;
    }

    public String getRegionalLimitations() {
        return regionalLimitations;
    }

    public void setRegionalLimitations(String regionalLimitations) {
        this.regionalLimitations = regionalLimitations;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getActivationDetails() {
        return activationDetails;
    }

    public void setActivationDetails(String activationDetails) {
        this.activationDetails = activationDetails;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Integer getOffersCount() {
        return offersCount;
    }

    public void setOffersCount(Integer offersCount) {
        this.offersCount = offersCount;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    @Id
    @Column(name = "kinguin_id")
    private Long kinguinId;

    @Column(name = "product_id", length = 255, nullable = false)
    private String productId;

    @Column(name = "name", length = 255, nullable = true)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "platform", length = 100)
    private String platform;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "text_qty")
    private Integer textQty;

    @Column(name = "regional_limitations", length = 500)
    private String regionalLimitations;

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "activation_details", length = 1000)
    private String activationDetails;

    @Column(name = "original_name", length = 500)
    private String originalName;

    @Column(name = "offers_count")
    private Integer offersCount;

    @Column(name = "total_qty")
    private Integer totalQty;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Column(name = "genres", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> genres;

    @Column(name = "age_rating", length = 100)
    private String ageRating;

    public List<String> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<String> developers) {
        this.developers = developers;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<String> publishers) {
        this.publishers = publishers;
    }

    @Column(name = "developers", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> developers;

    @Column(name = "publishers", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> publishers;

    @Column(name = "tags", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> tags;




    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }


        // Constructor vacío
        public GiftCardEntity() {}

    public List<String> getTags() {
        return this.tags;
    }

    // Getters y Setters (omitidos por brevedad)
    }
