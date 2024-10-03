package com.geekbank.bank.services;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.models.AccountStatus;
import com.geekbank.bank.models.VerificationStatus;
import com.geekbank.bank.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para manejo transaccional
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Crear una nueva cuenta
    public Account createAccount(Account account) {
        // Validaciones iniciales (si es necesario)

        // Inicializar campos predeterminados
        account.setStatus(AccountStatus.ACTIVE);
        account.setVerificationStatus(VerificationStatus.UNVERIFIED);
        account.setBalance(0.0);
        account.setLoyaltyPoints(0);

        // Generar un número de cuenta único
        account.setAccountNumber(generateUniqueAccountNumber());

        return accountRepository.save(account);
    }

    public List<Account> getAccountsByStatus(AccountStatus status) {
        return accountRepository.findByStatus(status);
    }


    // Obtener una cuenta por ID
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    // Obtener una cuenta por número de cuenta
    public Optional<Optional<Account>> getAccountByAccountNumber(String accountNumber) {
        return Optional.ofNullable(accountRepository.findByAccountNumber(accountNumber));
    }

    // Obtener todas las cuentas de un usuario específico
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    // Actualizar una cuenta existente
    public Account updateAccount(Account account) {
        // Asegurarse de que la cuenta existe
        if (!accountRepository.existsById(account.getId())) {
            throw new RuntimeException("La cuenta no existe");
        }
        return accountRepository.save(account);
    }

    // Eliminar una cuenta
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new RuntimeException("La cuenta no existe");
        }
        accountRepository.deleteById(id);
    }

    // Acumular puntos de lealtad
    @Transactional
    public void addLoyaltyPoints(Long accountId, int points) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        int newPoints = account.getLoyaltyPoints() + points;
        account.setLoyaltyPoints(newPoints);

        accountRepository.save(account);
    }

    // Debitar saldo de la cuenta
    @Transactional
    public void debitAccount(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (account.getBalance() < amount) {
            throw new RuntimeException("Fondos insuficientes");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    @Transactional
    public void creditAccount(Long accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    public void changeAccountStatus(Long accountId, AccountStatus status) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setStatus(status);
        accountRepository.save(account);
    }

    public void changeVerificationStatus(Long accountId, VerificationStatus verificationStatus) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        account.setVerificationStatus(verificationStatus);
        accountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {
        return UUID.randomUUID().toString();
    }

    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }
}
