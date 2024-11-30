package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Product;
import com.geekbank.bank.models.FreeFireDiamondProduct;
import com.geekbank.bank.models.FreeFireDiamondDenomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Obtener todos los productos que sean específicamente de tipo FreeFireDiamondProduct
    List<FreeFireDiamondProduct> findByDenominationNotNull();

    // Obtener productos por nombre
    Product findByName(String name);
    Optional<Product> findByProductId(Long productId);
    List<Product> findAll();

    // Obtener productos por rango de precio
    List<Product> findByPriceBetween(double minPrice, double maxPrice);

    // Obtener productos por denominación específica (solo para FreeFireDiamondProduct)
    List<FreeFireDiamondProduct> findByDenomination(FreeFireDiamondDenomination denomination);

    // Buscar productos por descripción parcial (usando like)
    List<Product> findByDescriptionContaining(String keyword);
    @Query("SELECT p FROM Product p WHERE TYPE(p) = FreeFireDiamondProduct")
    List<FreeFireDiamondProduct> findAllFreeFireDiamondProducts();

}
