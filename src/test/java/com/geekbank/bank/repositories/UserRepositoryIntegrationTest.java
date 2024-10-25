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
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository; // Inyectar el repositorio de cuentas

    @BeforeEach
    public void setup() {
        // Limpia las tablas antes de cada prueba
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Crear y guardar usuarios de prueba
        User user1 = new User();
        user1.setEmail("testuser1@example.com");
        user1.setEnabled(true);
        user1.setActivationToken("token-001");
        user1.setName("Test User One");
        user1.setPassword("{bcrypt}$2a$10$...");
        user1.setPhoneNumber("1112223333");
        user1.setRole(Roles.CUSTOMER);

        User user2 = new User();
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
    @DisplayName("Buscar usuario por email existente")
    public void testFindByEmail_ExistingEmail() {
        Optional<User> userOpt = userRepository.findByEmail("testuser1@example.com");
        assertTrue(userOpt.isPresent(), "El usuario debería existir");
        User user = userOpt.get();
        assertEquals("Test User One", user.getName(), "El nombre del usuario debería coincidir");
        assertTrue(user.isEnabled(), "El usuario debería estar habilitado");
    }

    @Test
    @DisplayName("Buscar usuario por email inexistente")
    public void testFindByEmail_NonExistingEmail() {
        Optional<User> userOpt = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(userOpt.isPresent(), "El usuario no debería existir");
    }

    @Test
    @DisplayName("Buscar usuario por ID existente")
    public void testFindById_ExistingId() {
        // Obtener un usuario existente
        User user = userRepository.findByEmail("testuser2@example.com").orElse(null);
        assertNotNull(user, "El usuario debería existir");
        Long userId = user.getId();

        Optional<User> retrievedUserOpt = userRepository.findById(userId);
        assertTrue(retrievedUserOpt.isPresent(), "El usuario debería ser encontrado por ID");
        User retrievedUser = retrievedUserOpt.get();
        assertEquals("testuser2@example.com", retrievedUser.getEmail(), "El email debería coincidir");
        assertFalse(retrievedUser.isEnabled(), "El usuario debería estar deshabilitado");
    }

    @Test
    @DisplayName("Buscar usuario por ID inexistente")
    public void testFindById_NonExistingId() {
        Optional<User> retrievedUserOpt = userRepository.findById(999L);
        assertFalse(retrievedUserOpt.isPresent(), "El usuario no debería existir");
    }

    @Test
    @DisplayName("Buscar usuario por token de activación existente")
    public void testFindByActivationToken_ExistingToken() {
        Optional<User> userOpt = userRepository.findByActivationToken("token-002");
        assertTrue(userOpt.isPresent(), "El usuario debería existir con el token proporcionado");
        User user = userOpt.get();
        assertEquals("testuser2@example.com", user.getEmail(), "El email debería coincidir");
        assertEquals("Test User Two", user.getName(), "El nombre del usuario debería coincidir");
    }

    @Test
    @DisplayName("Buscar usuario por token de activación inexistente")
    public void testFindByActivationToken_NonExistingToken() {
        Optional<User> userOpt = userRepository.findByActivationToken("invalid-token");
        assertFalse(userOpt.isPresent(), "No debería existir ningún usuario con el token proporcionado");
    }

    @Test
    @DisplayName("Buscar usuario por ID con cuenta")
    public void testFindByIdWithAccount() {
        // Crear una cuenta asociada a un usuario
        User user = userRepository.findByEmail("testuser1@example.com").orElse(null);
        assertNotNull(user, "El usuario debería existir");

        Account account = new Account();
        account.setAccountNumber("ACC123456");
        account.setBalance(1000.0);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account.setCurrency("USD");
        account.setAccountType(AccountType.SAVINGS);
        account.setDailyLimit(5000.0);
        account.setVerificationStatus(VerificationStatus.VERIFIED);
        account.setLoyaltyPoints(100);

        accountRepository.save(account);

        // Buscar el usuario con la cuenta cargada
        Optional<User> userWithAccountOpt = userRepository.findByIdWithAccount(user.getId());
        assertTrue(userWithAccountOpt.isPresent(), "El usuario debería ser encontrado con su cuenta");
        User retrievedUser = userWithAccountOpt.get();
        assertNotNull(retrievedUser.getAccount(), "La cuenta asociada no debería ser nula");
        assertEquals("ACC123456", retrievedUser.getAccount().getAccountNumber(), "El número de cuenta debería coincidir");
    }

    @Test
    @DisplayName("Buscar todos los usuarios con sus cuentas")
    public void testFindAllWithAccount() {
        // Crear cuentas asociadas a usuarios
        User user1 = userRepository.findByEmail("testuser1@example.com").orElse(null);
        User user2 = userRepository.findByEmail("testuser2@example.com").orElse(null);
        assertNotNull(user1, "El usuario1 debería existir");
        assertNotNull(user2, "El usuario2 debería existir");

        Account account1 = new Account();
        account1.setAccountNumber("ACC654321");
        account1.setBalance(2000.0);
        account1.setStatus(AccountStatus.INACTIVE);
        account1.setUser(user2);
        account1.setCurrency("EUR");
        account1.setAccountType(AccountType.SAVINGS);
        account1.setDailyLimit(10000.0);
        account1.setVerificationStatus(VerificationStatus.PENDING);
        account1.setLoyaltyPoints(200);

        accountRepository.save(account1);

        // Buscar todos los usuarios con sus cuentas
        List<User> usersWithAccounts = userRepository.findAllWithAccount();
        assertNotNull(usersWithAccounts, "La lista de usuarios no debería ser nula");
        assertEquals(2, usersWithAccounts.size(), "Debería haber dos usuarios en la lista");

        for (User u : usersWithAccounts) {
            if (u.getEmail().equals("testuser1@example.com")) {
                // Asumiendo que testuser1 tiene una cuenta
                assertNotNull(u.getAccount(), "El usuario1 debería tener una cuenta");
                assertEquals("ACC123456", u.getAccount().getAccountNumber(), "El número de cuenta debería coincidir");
            } else if (u.getEmail().equals("testuser2@example.com")) {
                // Asumiendo que testuser2 tiene una cuenta
                assertNotNull(u.getAccount(), "El usuario2 debería tener una cuenta");
                assertEquals("ACC654321", u.getAccount().getAccountNumber(), "El número de cuenta debería coincidir");
            }
        }
    }
}
