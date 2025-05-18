// VisitController.java
package com.geekbank.bank.metrics.visits.controller;

import com.geekbank.bank.metrics.visits.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    private VisitService visitService;
    @PostMapping("/register")
    public void registerVisit(@RequestParam String sessionId) {
        visitService.registerVisit(sessionId);
    }
    @GetMapping("/count")
    public long getVisitCount() {
        return visitService.getTotalVisits();
    }
}
