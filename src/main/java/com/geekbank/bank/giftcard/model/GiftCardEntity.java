package com.geekbank.bank.giftcard.model;

import com.geekbank.bank.support.converters.ListToStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "gift_cards")
@Getter
@Setter
public class GiftCardEntity {

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

    @Column(name = "developers", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> developers;

    @Column(name = "publishers", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> publishers;

    @Column(name = "tags", length = 500)
    @Convert(converter = ListToStringConverter.class)
    private List<String> tags;

}
