package com.geekbank.bank.giftcard.featured.highlight.controller;

import com.geekbank.bank.giftcard.featured.highlight.dto.HighlightDTO;
import com.geekbank.bank.giftcard.featured.highlight.model.HighlightItem;
import com.geekbank.bank.giftcard.featured.highlight.service.HighlightService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    @Autowired
    public HighlightController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @GetMapping
    public ResponseEntity<List<HighlightItem>> getHighlights() {
        List<HighlightItem> highlights = highlightService.fetchHighlights();
        return ResponseEntity.ok(highlights);
    }



    @PostMapping()
    public ResponseEntity<List<HighlightItem>> addHighlights(@RequestBody List<HighlightItem> highlightItems) {
        List<HighlightItem> addedHighlights = highlightService.addHighlightItems(highlightItems);
        return ResponseEntity.ok(addedHighlights);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeHighlights(@RequestBody List<Long> productIds) {
        highlightService.removeHighlightItems(productIds);
        return ResponseEntity.noContent().build();
    }

    @Getter
    @Setter
    public static class HighlightRequest {
        private List<HighlightDTO> highlightDTOS;
    }
}
