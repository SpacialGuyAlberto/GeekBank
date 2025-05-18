package com.geekbank.bank.giftcard.controller;

import com.geekbank.bank.giftcard.model.FreeFireDiamondProduct;
import com.geekbank.bank.giftcard.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/freefire")
public class FreeFireDiamondProductController {

    private final ProductService productService;

    @Autowired
    public FreeFireDiamondProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<FreeFireDiamondProduct> getAllFreeFireDiamondProducts() {
        return productService.getAllFreeFireDiamondProducts();
    }
}
