package com.geekbank.bank.controllers;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.Product;
import com.geekbank.bank.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

//    @GetMapping("/top-products")
//    public List<Product> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
//        return recommendationService.getTopProducts(limit);
//    }
//    @GetMapping("/user/{userId}")
//    public List<KinguinGiftCard> recommendByUserSimilarity(@PathVariable Long userId,
//                                                           @RequestParam(defaultValue = "5") int k) {
//        return recommendationService.recommendByUserSimilarity(userId, k);
//    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KinguinGiftCard>> getRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int k) {
        List<KinguinGiftCard> recommendations = recommendationService.recommendByUserSimilarity(userId, k);
        return ResponseEntity.ok(recommendations);  // Devuelve las recomendaciones, incluso si está vacío
    }
}
