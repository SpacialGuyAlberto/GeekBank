package com.geekbank.bank.giftcard;

import com.geekbank.bank.giftcard.model.Product;
import com.geekbank.bank.giftcard.model.FreeFireDiamondProduct;
import com.geekbank.bank.giftcard.constants.FreeFireDiamondDenomination;
import com.geekbank.bank.giftcard.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // Constructor que recibe ProductRepository
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Obtener todos los productos
    @Transactional
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Obtener todos los productos que sean de tipo FreeFireDiamondProduct
    public List<FreeFireDiamondProduct> getProductsByDenominationNotNull() {
        return productRepository.findByDenominationNotNull();
    }

    // Guardar un producto
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Obtener productos por denominación
    public List<FreeFireDiamondProduct> getProductsByDenomination(FreeFireDiamondDenomination denomination) {
        return productRepository.findByDenomination(denomination);
    }

    public List<FreeFireDiamondProduct> getAllFreeFireDiamondProducts() {
        return productRepository.findAllFreeFireDiamondProducts();
    }


    // Obtener productos por rango de precios
    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    // Buscar productos que contengan una palabra clave en la descripción
    public List<Product> searchProductsByDescription(String keyword) {
        return productRepository.findByDescriptionContaining(keyword);
    }
}
