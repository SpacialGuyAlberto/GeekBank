package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {

}
