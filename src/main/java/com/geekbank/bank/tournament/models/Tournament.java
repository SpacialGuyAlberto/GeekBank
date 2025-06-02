package com.geekbank.bank.tournament.models;

import com.geekbank.bank.tournament.player.Player;
import com.geekbank.bank.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    @Nullable
    @ManyToMany(mappedBy = "tournaments")
    private List<Player> players;

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    private User moderator;
}

