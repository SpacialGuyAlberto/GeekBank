package com.geekbank.bank.tournament.dto;

import com.geekbank.bank.tournament.player.Player;
import com.geekbank.bank.user.model.User;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
public class TournamentDTO {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @Nullable
    private String status;
    @Nullable
    private List<Player> players;
    private Long moderatorId;
}
