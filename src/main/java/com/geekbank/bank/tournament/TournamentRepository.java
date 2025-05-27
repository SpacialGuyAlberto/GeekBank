package com.geekbank.bank.tournament;

import com.geekbank.bank.tournament.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(String status);

    List<Tournament> findByNameContainingIgnoreCase(String name);

}

