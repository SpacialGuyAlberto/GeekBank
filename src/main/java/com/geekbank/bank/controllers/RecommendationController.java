package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Product;
import com.geekbank.bank.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
