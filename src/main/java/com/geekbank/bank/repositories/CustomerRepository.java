package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Aquí puedes definir métodos adicionales si es necesario
    // Spring Data JPA proporcionará la implementación de estos métodos automáticamente
}

