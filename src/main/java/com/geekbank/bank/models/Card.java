package com.geekbank.bank.models;
import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Card {
    @Id
    private Long id;
    private String cardNumber;
    private LocalDate expiration;
    private String securityCode;
    private String cardType;

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
    public void setCardNumber(String cardNumber){
        this.cardNumber = cardNumber;
    }
    public String getCardNumber(){
        return cardNumber;
    }
    public void setExpiration(LocalDate expiration){
        this.expiration = expiration;
    }
    public LocalDate getExpiration(){
        return expiration;
    }


}
