package com.geekbank.bank.user.repository;

import com.geekbank.bank.user.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Aquí puedes definir métodos adicionales si es necesario
    // Spring Data JPA proporcionará la implementación de estos métodos automáticamente
}

