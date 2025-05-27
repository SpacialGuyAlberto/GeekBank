package com.geekbank.bank.tournament.prize.service;

import com.geekbank.bank.tournament.prize.Prize;
import com.geekbank.bank.tournament.prize.PrizeRepository;
import com.geekbank.bank.tournament.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrizeService {

    @Autowired
    private PrizeRepository prizeRepository;

    public List<Prize> getAllPrizes() {
        return prizeRepository.findAll();
    }

    public Optional<Prize> getPrizeById(Long id) {
        return prizeRepository.findById(id);
    }

    public Prize savePrize(Prize prize) {
        return prizeRepository.save(prize);
    }

    public void deletePrize(Long id) {
        prizeRepository.deleteById(id);
    }

    public List<Prize> getPrizesByTournament(Long tournamentId) {
        return prizeRepository.findByTournamentId(tournamentId);
    }

    public List<Prize> getPrizesByWinner(Player player) {
        return prizeRepository.findByWinner(player);
    }

}
