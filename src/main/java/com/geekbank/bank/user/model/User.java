package com.geekbank.bank.user.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.promotion.model.Promotion;
import com.geekbank.bank.user.constants.Roles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.security.SecureRandom;
import java.util.Base64;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @NaturalId(mutable = true)
    @Column(nullable = false, unique = true)
    private String email;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Account account;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String phoneNumber;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Roles role;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = false;

    @Column(nullable = true)
    private String activationToken;

    @Column(nullable = true, unique = true)
    private String affiliateLink;

    @Column(nullable = true, unique = true)
    private String promoCode;

    @Column(nullable = true)
    private Double commissionRate;

    @PrimaryKeyJoinColumn
    @OneToOne
    private Promotion promo;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    @PrePersist
    protected void onCreate() {
        if (this.activationToken == null) {
            this.activationToken = generateActivationToken();
        }
    }

    private String generateActivationToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
