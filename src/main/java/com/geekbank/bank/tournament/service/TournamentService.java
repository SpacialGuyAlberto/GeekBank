package com.geekbank.bank.tournament.service;

import com.geekbank.bank.tournament.models.Tournament;
import com.geekbank.bank.tournament.dto.TournamentDTO;
import com.geekbank.bank.tournament.models.TournamentRepository;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;
    private final UserService userService;

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

    public void createTournament(TournamentDTO dto) {
        Tournament newTournament = new Tournament();
        newTournament.setName(dto.getName());
        newTournament.setDescription(dto.getDescription());
        newTournament.setStatus(dto.getStatus());
        newTournament.setStartDate(dto.getStartDate());
        newTournament.setEndDate(dto.getEndDate());

        // Busca el moderador por ID
        User moderator = userService.getUserById(dto.getModeratorId())
                .orElseThrow(() -> new IllegalArgumentException("Moderador no encontrado"));
        newTournament.setModerator(moderator);

        tournamentRepository.save(newTournament);
    }


}
