package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Encontrar cuenta por número de cuenta
    Optional<Account> findByAccountNumber(String accountNumber);

    // Encontrar todas las cuentas de un usuario
    List<Account> findByUserId(Long userId);

    // Consultar todas las cuentas con un balance mayor que una cantidad dada
    List<Account> findByBalanceGreaterThan(double amount);

    // Consultar todas las cuentas con un balance menor que una cantidad dada
    List<Account> findByBalanceLessThan(double amount);
}
