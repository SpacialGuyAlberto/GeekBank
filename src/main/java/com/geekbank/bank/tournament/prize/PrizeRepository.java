package com.geekbank.bank.tournament.prize;

import com.geekbank.bank.tournament.prize.Prize;
import com.geekbank.bank.tournament.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {

    List<Prize> findByTournamentId(Long tournamentId);

    List<Prize> findByWinner(Player player);

}

