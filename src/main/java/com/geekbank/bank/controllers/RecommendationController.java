package com.geekbank.bank.controllers;

import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.Product;
import com.geekbank.bank.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
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
            @RequestParam(defaultValue = "4") int k) {
        List<KinguinGiftCard> recommendations = recommendationService.recommend(userId, k);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<KinguinGiftCard>> getMostPopular(
            @RequestParam(defaultValue = "4") int k )
    {
        List<KinguinGiftCard> recommendations = recommendationService.recommendByPopularity(k);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/content-based/{productId}")
    public ResponseEntity<List<KinguinGiftCard>> getContentBasedRecommendations(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "20") int limit) {

        List<KinguinGiftCard> recommendations = recommendationService.recommendByContent(productId, limit);
        return ResponseEntity.ok(recommendations);
    }
}
