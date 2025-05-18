package com.geekbank.bank.payment.stripe.model;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Card {
    @Id
    private Long id;
    private String cardNumber;
    private LocalDate expiration;
    private String securityCode;
    private String cardType;
}
