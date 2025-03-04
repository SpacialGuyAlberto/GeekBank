package com.geekbank.bank.controllers;


import com.geekbank.bank.models.Promotion;
import com.geekbank.bank.repositories.PromotionRepository;
import com.geekbank.bank.services.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {
    private PromotionRepository promotionRepository;
    private final PromoService promoService;

    @Autowired
    public PromotionController(PromoService promoService){
        this.promoService = promoService;
    }

    @GetMapping("/checkIfCodeExists/{Code}")
    public ResponseEntity<Boolean> checkIfCodeExists( @PathVariable String Code){
        Boolean codeExists = promoService.checkIfExists(Code);
        return ResponseEntity.ok(codeExists);
    }
}
