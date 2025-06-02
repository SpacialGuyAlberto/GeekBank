package com.geekbank.bank.tournament.controller;

import com.geekbank.bank.tournament.dto.TournamentInscriptionDTO;
import com.geekbank.bank.tournament.dto.TournamentDTO;
import com.geekbank.bank.tournament.service.TournamentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournament")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class TournamentController {


    private final TournamentService tournamentService;


    @PostMapping("/send")
    public ResponseEntity<String> receiveTournamentInscription(@RequestBody TournamentInscriptionDTO inscription) {

        if (inscription.getNickname() == null || inscription.getNickname().isEmpty()) {
            return ResponseEntity.badRequest().body("El nickname es obligatorio.");
        }
        if (inscription.getEmail() == null || !inscription.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body("El email no es válido.");
        }
        if (inscription.getGamerId() == null || inscription.getGamerId().isEmpty()) {
            return ResponseEntity.badRequest().body("El gamerId es obligatorio.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("¡Inscripción recibida con éxito!");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTournament(@RequestBody TournamentDTO tournament) {
        tournamentService.createTournament(tournament);
        return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
    }


}

