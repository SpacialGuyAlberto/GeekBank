package com.geekbank.bank.flashoffers.controller;

import com.geekbank.bank.flashoffers.model.FlashOffer;
import com.geekbank.bank.flashoffers.service.FlashOfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-offers")
public class FlashOfferController {

    private final FlashOfferService service;

    public FlashOfferController(FlashOfferService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FlashOffer>> getAllOffers() {
        return ResponseEntity.ok(service.getAllOffers());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<FlashOffer> getOfferByProduct(@PathVariable Long productId) {
        return service.getOfferByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FlashOffer> createOffer(@RequestBody FlashOffer offer) {
        FlashOffer created = service.createOffer(offer);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashOffer> updateOffer(@PathVariable Long id, @RequestBody FlashOffer offer) {
        return ResponseEntity.ok(service.updateOffer(id, offer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        service.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
