package com.geekbank.bank.auth;

import com.geekbank.bank.user.model.User;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class PasswordResetToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity =  User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
