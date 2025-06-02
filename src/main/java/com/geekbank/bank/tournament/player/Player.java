package com.geekbank.bank.tournament.player;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.geekbank.bank.tournament.models.Tournament;
import com.geekbank.bank.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "players")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Player extends User {

    @ManyToMany
    @JoinTable(
            name = "tournament_player",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )

    @Nullable
    private List<Tournament> tournaments;
    @Nullable
    private String gamePlayerId;
    @Nullable
    private String gamePlayerName;
    @Nullable
    private String teamName;
}

