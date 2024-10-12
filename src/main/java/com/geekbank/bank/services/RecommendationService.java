package com.geekbank.bank.services;

import com.geekbank.bank.models.Product;
import com.geekbank.bank.repositories.ProductRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

//    public List<Product> getTopProducts(int limit) {
//        List<Object[]> results = transactionRepository.findTopPurchasedProducts();
//
//        List<String> productIds = results.stream()
//                .map(row -> (String) row[0])
//                .limit(limit)
//                .collect(Collectors.toList());
//
//        return productRepository.findAllById(productIds);
//    }
}

