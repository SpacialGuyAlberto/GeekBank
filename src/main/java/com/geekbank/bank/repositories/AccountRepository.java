package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.models.AccountStatus;
import com.geekbank.bank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAll();
    Optional<Account> findByUserId(Long userId);
    Account findFirstByUserId(Long userId);
    Optional<Account> findByUser(User user);

    List<Account> findByBalanceGreaterThan(double amount);

    List<Account> findByBalanceLessThan(double amount);
    List<Account> findByStatus(AccountStatus status);
}
