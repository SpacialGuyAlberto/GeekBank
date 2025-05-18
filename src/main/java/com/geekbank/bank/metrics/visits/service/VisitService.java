package com.geekbank.bank.metrics.visits.service;
import com.geekbank.bank.metrics.visits.repository.VisitRepository;
import com.geekbank.bank.metrics.visits.model.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VisitService {

    @Autowired
    private VisitRepository visitRepository;
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    public void registerVisit(String sessionId) {
        Visit lastVisit = visitRepository.findTopBySessionIdOrderByTimestampDesc(sessionId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (lastVisit == null || lastVisit.getTimestamp().plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(now)) {
            Visit visit = new Visit();
            visit.setSessionId(sessionId);
            visit.setTimestamp(now);
            visitRepository.save(visit);
        }
    }

    public long getTotalVisits() {
        return visitRepository.count();
    }
}
