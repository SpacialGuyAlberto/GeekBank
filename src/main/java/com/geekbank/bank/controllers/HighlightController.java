package com.geekbank.bank.controllers;

import com.geekbank.bank.models.HighlightItem;
import com.geekbank.bank.models.HighlightItemWithGiftcardDTO;
import com.geekbank.bank.services.HighlightService;
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
    public ResponseEntity<List<HighlightItemWithGiftcardDTO>> getHighlights() {
        List<HighlightItemWithGiftcardDTO> highlights = highlightService.getHighlightsByProductIds();
        return ResponseEntity.ok(highlights);
    }

    @PostMapping()
    public ResponseEntity<List<HighlightItem>> addHighlights(@RequestBody HighlightRequest request) {
        List<HighlightItem> addedHighlights = highlightService.addHighlightItems(request.getProductIds());
        return ResponseEntity.ok(addedHighlights);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeHighlights(@RequestBody List<Long> productIds) {
        highlightService.removeHighlightItems(productIds);
        return ResponseEntity.noContent().build();
    }

    // Clase para encapsular la lista de productIds
    public static class HighlightRequest {
        private List<Long> productIds;

        public List<Long> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }
    }
}
