package com.geekbank.bank.controllers;

import com.geekbank.bank.models.ActivationDetails;
import com.geekbank.bank.services.ActivationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/activation-details")
public class ActivationDetailsController {

    @Autowired
    private ActivationDetailsService activationDetailsService;

    /**
     * Crear o actualizar los detalles de activación para un producto externo (kinguinId).
     * Se envían los datos vía JSON en el body.
     */
    @PostMapping
    public ResponseEntity<ActivationDetails> createOrUpdate(
            @RequestBody ActivationDetails body
    ) {
        if (body.getKinguinId() == null) {
            return ResponseEntity.badRequest().build();
        }

        ActivationDetails updated = activationDetailsService.createOrUpdateActivationDetails(
                body.getKinguinId(),
                body.getVideoUrl(),
                body.getTextDetails()
        );
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{kinguinId}")
    public ResponseEntity<ActivationDetails> getDetails(@PathVariable Long kinguinId) {
        return activationDetailsService.getDetailsByKinguinId(kinguinId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{kinguinId}")
    public ResponseEntity<Void> deleteDetails(@PathVariable Long kinguinId) {
        activationDetailsService.deleteDetailsByKinguinId(kinguinId);
        return ResponseEntity.ok().build();
    }
}


