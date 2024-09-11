package com.geekbank.bank.services;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

//    @Transactional
//    public void updateAccountBalance(Long userId, double amount) {
//        Account account = accountRepository.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
//
//        // Actualizar el saldo de la cuenta
//        account.setBalance(account.getBalance() - amount);
//        accountRepository.save(account);
//    }
    public Account updateAccountBalance(Long accountId, double newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        account.setBalance(newBalance);
        return accountRepository.save(account);
    }
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public List<Account> getAccountsByBalanceGreaterThan(double amount) {
        return accountRepository.findByBalanceGreaterThan(amount);
    }
}
