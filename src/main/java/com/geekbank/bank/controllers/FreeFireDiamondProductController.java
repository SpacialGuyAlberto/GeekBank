package com.geekbank.bank.controllers;

import com.geekbank.bank.models.FreeFireDiamondProduct;
import com.geekbank.bank.models.Product;
import com.geekbank.bank.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/freefire")
public class FreeFireDiamondProductController {

    private final ProductService productService;

    @Autowired
    public FreeFireDiamondProductController(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint para obtener todos los productos de Free Fire
    @GetMapping("/products")
    public List<FreeFireDiamondProduct> getAllFreeFireDiamondProducts() {
        return productService.getAllFreeFireDiamondProducts();
    }

}
