package com.geekbank.bank.combo.controller;

import com.geekbank.bank.combo.dto.ComboRequest;
import com.geekbank.bank.combo.model.ComboEntity;
import com.geekbank.bank.combo.service.ComboService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
public class ComboController {

    private final ComboService comboService;

    public ComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping
    public ResponseEntity<List<ComboEntity>> getAllCombos() {
        return ResponseEntity.ok(comboService.getAllActiveCombos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComboEntity> getComboById(@PathVariable Long id) {
        return ResponseEntity.ok(comboService.getComboById(id));
    }

    @PostMapping
    public ResponseEntity<?> createCombo(@RequestBody ComboRequest request) {
        try {
            ComboEntity combo = new ComboEntity();
            combo.setName(request.getName());
            combo.setDescription(request.getDescription());
            combo.setPrice(request.getPrice());
            combo.setImageUrl(request.getImageUrl());
            combo.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

            ComboEntity createdCombo = comboService.createCombo(combo, request.getProductIds());
            return ResponseEntity.ok(createdCombo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCombo(@PathVariable Long id, @RequestBody ComboRequest request) {
        try {
            ComboEntity comboUpdates = new ComboEntity();
            comboUpdates.setName(request.getName());
            comboUpdates.setDescription(request.getDescription());
            comboUpdates.setPrice(request.getPrice());
            comboUpdates.setImageUrl(request.getImageUrl());
            comboUpdates.setIsActive(request.getIsActive());

            ComboEntity updatedCombo = comboService.updateCombo(id, comboUpdates, request.getProductIds());
            return ResponseEntity.ok(updatedCombo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return ResponseEntity.noContent().build();
    }
}
