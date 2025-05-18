package com.geekbank.bank.services;

import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.user.account.constants.AccountStatus;
import com.geekbank.bank.user.account.service.AccountService;
import com.geekbank.bank.user.constants.Roles;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.account.repository.AccountRepository;
import com.geekbank.bank.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Crear un usuario para asociar con la cuenta
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setPassword("{bcrypt}$2a$10$...");
        testUser.setRole(Roles.CUSTOMER);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Crear una cuenta y verificar que se guarda correctamente en la base de datos")
    public void testCreateAccount() {
        Account account = new Account();
        account.setUser(testUser);
        account.setCurrency("USD");

        Account createdAccount = accountService.createAccount(account);

        assertNotNull(createdAccount.getId(), "La cuenta debería tener un ID después de guardarse");
        assertEquals(0.0, createdAccount.getBalance(), "El balance inicial debería ser 0.0");
        assertEquals(AccountStatus.ACTIVE, createdAccount.getStatus(), "El estado inicial de la cuenta debería ser ACTIVE");
    }

    @Test
    @DisplayName("Actualizar el balance de una cuenta")
    public void testUpdateAccountBalance() {
        Account account = new Account();
        account.setUser(testUser);
        account.setCurrency("USD");
        account = accountService.createAccount(account);

        accountService.creditAccount(account.getId(), 500.0);
        Optional<Account> updatedAccountOpt = accountService.getAccountById(account.getId());

        assertTrue(updatedAccountOpt.isPresent(), "La cuenta debería existir después de la actualización");
        Account updatedAccount = updatedAccountOpt.get();
        assertEquals(500.0, updatedAccount.getBalance(), "El balance de la cuenta debería ser 500.0 después del crédito");
    }

    @Test
    @DisplayName("Añadir puntos de lealtad a una cuenta")
    public void testAddLoyaltyPoints() {
        Account account = new Account();
        account.setUser(testUser);
        account.setCurrency("USD");
        account = accountService.createAccount(account);

        accountService.addLoyaltyPoints(account.getId(), 10);
        Optional<Account> updatedAccountOpt = accountService.getAccountById(account.getId());

        assertTrue(updatedAccountOpt.isPresent(), "La cuenta debería existir después de añadir puntos de lealtad");
        Account updatedAccount = updatedAccountOpt.get();
        assertEquals(10, updatedAccount.getLoyaltyPoints(), "Los puntos de lealtad de la cuenta deberían ser 10");
    }
}
