package com.geekbank.bank.promotion.controller;
import com.geekbank.bank.promotion.model.Promotion;
import com.geekbank.bank.promotion.repository.PromotionRepository;
import com.geekbank.bank.promotion.service.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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

    @GetMapping("/fetch-code/{Code}")
    public ResponseEntity<Optional<Promotion>> getPromotionByCode(@PathVariable String Code){
        Optional<Promotion> promotion = Optional.ofNullable(promoService.getPromoByCode(Code));
        return ResponseEntity.ok(promotion);
    }
}
