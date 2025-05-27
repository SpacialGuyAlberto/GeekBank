package com.geekbank.bank.tournament.service;

import com.geekbank.bank.tournament.Tournament;
import com.geekbank.bank.tournament.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    public Optional<Tournament> getTournamentById(Long id) {
        return tournamentRepository.findById(id);
    }

    public Tournament saveTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    public void deleteTournament(Long id) {
        tournamentRepository.deleteById(id);
    }

    public List<Tournament> findTournamentsByStatus(String status) {
        return tournamentRepository.findByStatus(status);
    }

    public List<Tournament> searchTournamentsByName(String name) {
        return tournamentRepository.findByNameContainingIgnoreCase(name);
    }

}
