package com.geekbank.bank.repositories;

import com.geekbank.bank.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Indica que se usará application-test.properties
public class AccountRepositoryIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository; // Inyectar el repositorio de usuarios

    private User user1;
    private User user2;

    @BeforeEach
    public void setup() {
        // Limpia las tablas antes de cada prueba
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Crear y guardar usuarios de prueba
        user1 = new User();
        user1.setEmail("testuser1@example.com");
        user1.setEnabled(true);
        user1.setActivationToken("token-001");
        user1.setName("Test User One");
        user1.setPassword("{bcrypt}$2a$10$...");
        user1.setPhoneNumber("1112223333");
        user1.setRole(Roles.CUSTOMER);

        user2 = new User();
        user2.setEmail("testuser2@example.com");
        user2.setEnabled(false);
        user2.setActivationToken("token-002");
        user2.setName("Test User Two");
        user2.setPassword("{bcrypt}$2a$10$...");
        user2.setPhoneNumber("4445556666");
        user2.setRole(Roles.CUSTOMER);

        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    @DisplayName("Buscar cuenta por número de cuenta existente")
    public void testFindByAccountNumber_ExistingAccountNumber() {
        // Crear y guardar una cuenta asociada a user1
        Account account = new Account();
        account.setAccountNumber("ACC123456");
        account.setBalance(1500.0);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user1);
        account.setCurrency("USD");
        account.setAccountType(AccountType.SAVINGS);
        account.setDailyLimit(5000.0);
        account.setVerificationStatus(VerificationStatus.VERIFIED);
        account.setLoyaltyPoints(100);

        accountRepository.save(account);

        // Ejecutar la búsqueda
        Optional<Account> retrievedAccountOpt = accountRepository.findByAccountNumber("ACC123456");
        assertTrue(retrievedAccountOpt.isPresent(), "La cuenta debería existir");
        Account retrievedAccount = retrievedAccountOpt.get();
        assertEquals("ACC123456", retrievedAccount.getAccountNumber(), "El número de cuenta debería coincidir");
        assertEquals(1500.0, retrievedAccount.getBalance(), 0.001, "El balance debería coincidir");
        assertEquals(AccountStatus.ACTIVE, retrievedAccount.getStatus(), "El estado debería coincidir");
        assertEquals(user1.getId(), retrievedAccount.getUser().getId(), "El ID del usuario debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuenta por número de cuenta inexistente")
    public void testFindByAccountNumber_NonExistingAccountNumber() {
        Optional<Account> retrievedAccountOpt = accountRepository.findByAccountNumber("NONEXISTENT");
        assertFalse(retrievedAccountOpt.isPresent(), "La cuenta no debería existir");
    }

    @Test
    @DisplayName("Buscar todas las cuentas")
    public void testFindAll_ReturnsAllAccounts() {
        // Crear y guardar cuentas
        Account account1 = new Account();
        account1.setAccountNumber("ACC123456");
        account1.setBalance(1500.0);
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);
        account1.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(5000.0);
        account1.setVerificationStatus(VerificationStatus.VERIFIED);
        account1.setLoyaltyPoints(100);

        Account account2 = new Account();
        account2.setAccountNumber("ACC654321");
        account2.setBalance(2500.0);
        account2.setStatus(AccountStatus.INACTIVE);
        account2.setUser(user2);
        account2.setCurrency("EUR");
        account2.setAccountType(AccountType.SAVINGS);
        account2.setDailyLimit(10000.0);
        account2.setVerificationStatus(VerificationStatus.PENDING);
        account2.setLoyaltyPoints(200);

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Ejecutar la búsqueda
        List<Account> accounts = accountRepository.findAll();
        assertNotNull(accounts, "La lista de cuentas no debería ser nula");
        assertEquals(2, accounts.size(), "Debería haber dos cuentas en la lista");
    }

    @Test
    @DisplayName("Buscar cuenta por ID de usuario existente")
    public void testFindByUserId_ExistingUserId() {
        // Crear y guardar una cuenta asociada a user1
        Account account = new Account();
        account.setAccountNumber("ACC123456");
        account.setBalance(1500.0);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user1);
        account.setCurrency("USD");
        account.setAccountType(AccountType.SAVINGS);
        account.setDailyLimit(5000.0);
        account.setVerificationStatus(VerificationStatus.VERIFIED);
        account.setLoyaltyPoints(100);

        accountRepository.save(account);

        // Ejecutar la búsqueda
        Account retrievedAccount = accountRepository.findByUserId(user1.getId());
        assertNotNull(retrievedAccount, "La cuenta debería existir para el usuario");
        assertEquals("ACC123456", retrievedAccount.getAccountNumber(), "El número de cuenta debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuenta por ID de usuario inexistente")
    public void testFindByUserId_NonExistingUserId() {
        Account retrievedAccount = accountRepository.findByUserId(999L);
        assertNull(retrievedAccount, "No debería existir ninguna cuenta para el ID de usuario proporcionado");
    }

    @Test
    @DisplayName("Buscar primera cuenta por ID de usuario existente")
    public void testFindFirstByUserId_ExistingUserId() {
        // Crear y guardar múltiples cuentas para user1
        Account account1 = new Account();
        account1.setAccountNumber("ACC123456");
        account1.setBalance(1500.0);
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);
        account1.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(5000.0);
        account1.setVerificationStatus(VerificationStatus.VERIFIED);
        account1.setLoyaltyPoints(100);

        Account account2 = new Account();
        account2.setAccountNumber("ACC123457");
        account2.setBalance(2000.0);
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setUser(user1);
        account2.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account2.setDailyLimit(6000.0);
        account2.setVerificationStatus(VerificationStatus.VERIFIED);
        account2.setLoyaltyPoints(150);

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Ejecutar la búsqueda
        Account firstAccount = accountRepository.findFirstByUserId(user1.getId());
        assertNotNull(firstAccount, "La primera cuenta debería existir para el usuario");
        assertEquals("ACC123456", firstAccount.getAccountNumber(), "El número de cuenta de la primera cuenta debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuenta por objeto User existente")
    public void testFindByUser_ExistingUser() {
        // Crear y guardar una cuenta asociada a user2
        Account account = new Account();
        account.setAccountNumber("ACC654321");
        account.setBalance(2500.0);
        account.setStatus(AccountStatus.INACTIVE);
        account.setUser(user2);
        account.setCurrency("EUR");
        account.setAccountType(AccountType.SAVINGS);
        account.setDailyLimit(10000.0);
        account.setVerificationStatus(VerificationStatus.PENDING);
        account.setLoyaltyPoints(200);

        accountRepository.save(account);

        // Ejecutar la búsqueda
        Optional<Account> retrievedAccountOpt = accountRepository.findByUser(user2);
        assertTrue(retrievedAccountOpt.isPresent(), "La cuenta debería existir para el usuario");
        Account retrievedAccount = retrievedAccountOpt.get();
        assertEquals("ACC654321", retrievedAccount.getAccountNumber(), "El número de cuenta debería coincidir");
        assertEquals(AccountStatus.INACTIVE, retrievedAccount.getStatus(), "El estado debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuenta por objeto User inexistente")
    public void testFindByUser_NonExistingUser() {
        // Crear un usuario sin cuenta
        User user3 = new User();
        user3.setEmail("testuser3@example.com");
        user3.setEnabled(true);
        user3.setActivationToken("token-003");
        user3.setName("Test User Three");
        user3.setPassword("{bcrypt}$2a$10$...");
        user3.setPhoneNumber("7778889999");
        user3.setRole(Roles.CUSTOMER);

        userRepository.save(user3);

        // Ejecutar la búsqueda
        Optional<Account> retrievedAccountOpt = accountRepository.findByUser(user3);
        assertFalse(retrievedAccountOpt.isPresent(), "No debería existir ninguna cuenta para el usuario proporcionado");
    }

    @Test
    @DisplayName("Buscar cuentas con balance mayor a una cantidad específica")
    public void testFindByBalanceGreaterThan() {
        // Crear y guardar cuentas
        Account account1 = new Account();
        account1.setAccountNumber("ACC123456");
        account1.setBalance(1500.0);
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);
        account1.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(5000.0);
        account1.setVerificationStatus(VerificationStatus.VERIFIED);
        account1.setLoyaltyPoints(100);

        Account account2 = new Account();
        account2.setAccountNumber("ACC654321");
        account2.setBalance(2500.0);
        account2.setStatus(AccountStatus.INACTIVE);
        account2.setUser(user2);
        account2.setCurrency("EUR");
        account1.setAccountType(AccountType.SAVINGS);
        account2.setDailyLimit(10000.0);
        account2.setVerificationStatus(VerificationStatus.PENDING);
        account2.setLoyaltyPoints(200);

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Ejecutar la búsqueda
        List<Account> accounts = accountRepository.findByBalanceGreaterThan(2000.0);
        assertNotNull(accounts, "La lista de cuentas no debería ser nula");
        assertEquals(1, accounts.size(), "Debería haber una cuenta con balance mayor a 2000.0");
        assertEquals("ACC654321", accounts.get(0).getAccountNumber(), "El número de cuenta debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuentas con balance menor a una cantidad específica")
    public void testFindByBalanceLessThan() {
        // Crear y guardar cuentas
        Account account1 = new Account();
        account1.setAccountNumber("ACC123456");
        account1.setBalance(1500.0);
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);
        account1.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(5000.0);
        account1.setVerificationStatus(VerificationStatus.VERIFIED);
        account1.setLoyaltyPoints(100);

        Account account2 = new Account();
        account2.setAccountNumber("ACC654321");
        account2.setBalance(2500.0);
        account2.setStatus(AccountStatus.INACTIVE);
        account2.setUser(user2);
        account2.setCurrency("EUR");
        account1.setAccountType(AccountType.SAVINGS);
        account2.setDailyLimit(10000.0);
        account2.setVerificationStatus(VerificationStatus.PENDING);
        account2.setLoyaltyPoints(200);

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Ejecutar la búsqueda
        List<Account> accounts = accountRepository.findByBalanceLessThan(2000.0);
        assertNotNull(accounts, "La lista de cuentas no debería ser nula");
        assertEquals(1, accounts.size(), "Debería haber una cuenta con balance menor a 2000.0");
        assertEquals("ACC123456", accounts.get(0).getAccountNumber(), "El número de cuenta debería coincidir");
    }

    @Test
    @DisplayName("Buscar cuentas por estado")
    public void testFindByStatus() {
        // Crear y guardar cuentas
        Account account1 = new Account();
        account1.setAccountNumber("ACC123456");
        account1.setBalance(1500.0);
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);
        account1.setCurrency("USD");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(5000.0);
        account1.setVerificationStatus(VerificationStatus.VERIFIED);
        account1.setLoyaltyPoints(100);

        Account account2 = new Account();
        account2.setAccountNumber("ACC654321");
        account2.setBalance(2500.0);
        account2.setStatus(AccountStatus.INACTIVE);
        account2.setUser(user2);
        account2.setCurrency("EUR");
        account1.setAccountType(AccountType.SAVINGS);
        account2.setDailyLimit(10000.0);
        account2.setVerificationStatus(VerificationStatus.PENDING);
        account2.setLoyaltyPoints(200);

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Ejecutar la búsqueda por estado ACTIVE
        List<Account> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);
        assertNotNull(activeAccounts, "La lista de cuentas activas no debería ser nula");
        assertEquals(1, activeAccounts.size(), "Debería haber una cuenta activa");
        assertEquals("ACC123456", activeAccounts.get(0).getAccountNumber(), "El número de cuenta debería coincidir");

        // Ejecutar la búsqueda por estado INACTIVE
        List<Account> inactiveAccounts = accountRepository.findByStatus(AccountStatus.INACTIVE);
        assertNotNull(inactiveAccounts, "La lista de cuentas inactivas no debería ser nula");
        assertEquals(1, inactiveAccounts.size(), "Debería haber una cuenta inactiva");
        assertEquals("ACC654321", inactiveAccounts.get(0).getAccountNumber(), "El número de cuenta debería coincidir");
    }
}
