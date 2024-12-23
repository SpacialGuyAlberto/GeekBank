package com.geekbank.bank.controllers;

import com.geekbank.bank.models.ProgressResponse;
import com.geekbank.bank.models.SynchronizedResponse;
import com.geekbank.bank.models.TotalResponse;
import com.geekbank.bank.services.GiftCardSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private GiftCardSyncService giftCardSyncService;

    @PostMapping("/giftcards")
    public ResponseEntity<Map<String, String>> syncGiftCards() {
        giftCardSyncService.syncGiftCards();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sincronización de GiftCards iniciada.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress() {
        int progress = GiftCardSyncService.getProgress();
        ProgressResponse response = new ProgressResponse(progress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        boolean isSyncing = GiftCardSyncService.isSyncing();
        return ResponseEntity.ok(Collections.singletonMap("isSyncing", isSyncing));
    }

    @GetMapping("/insert-test")
    public String insertTestGiftCard() {
        giftCardSyncService.insertTestGiftCard();
        return "GiftCard de prueba insertada.";
    }


    @GetMapping("/total")
    public ResponseEntity<TotalResponse> getTotalGiftCards() {
        int total = GiftCardSyncService.getTotalGiftCards();
        TotalResponse response = new TotalResponse(total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/synchronized")
    public ResponseEntity<SynchronizedResponse> getSynchronizedGiftCards() {
        int synchronizedCount = GiftCardSyncService.getSynchronizedGiftCards();
        SynchronizedResponse response = new SynchronizedResponse(synchronizedCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelSync() {
        giftCardSyncService.cancelSync();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sincronización de GiftCards cancelada.");
        return ResponseEntity.ok(response);
    }
}
