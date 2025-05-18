package com.geekbank.bank.services;

import com.geekbank.bank.config.TestConfig;
import com.geekbank.bank.user.account.constants.AccountStatus;
import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.user.account.repository.AccountRepository;
import com.geekbank.bank.user.constants.Roles;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.repository.UserRepository;
import com.geekbank.bank.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

@Import(TestConfig.class)  // Importar la configuración de prueba
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Crear usuario y guardar en la base de datos")
    public void testCreateUser() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setPassword("{bcrypt}$2a$10$...");
        user.setPhoneNumber("1234567890");
        user.setRole(Roles.CUSTOMER);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser.getId(), "El ID del usuario creado no debería ser nulo");
        assertEquals("testuser@example.com", createdUser.getEmail(), "El email del usuario debería coincidir");
    }

    @Test
    @DisplayName("Registrar usuario y crear cuenta asociada")
    public void testRegisterUser() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setPassword("{bcrypt}$2a$10$...");
        user.setRole(Roles.CUSTOMER);

        userService.registerUser(user);

        Optional<User> registeredUserOpt = userRepository.findByEmail("testuser@example.com");
        assertTrue(registeredUserOpt.isPresent(), "El usuario registrado debería estar presente en la base de datos");

        User registeredUser = registeredUserOpt.get();
        assertFalse(registeredUser.isEnabled(), "El usuario no debería estar habilitado después del registro");
        assertNotNull(registeredUser.getActivationToken(), "El token de activación no debería ser nulo");

        Account account = accountRepository.findFirstByUserId(registeredUser.getId());
        assertNotNull(account, "La cuenta asociada debería estar creada para el usuario");
        assertEquals(0.0, account.getBalance(), "El balance inicial de la cuenta debería ser 0.0");
    }

    @Test
    @DisplayName("Activar usuario con token válido")
    public void testActivateUser_Success() {
        User user = new User();
        user.setEmail("activateuser@example.com");
        user.setName("Activate User");
        user.setPassword("{bcrypt}$2a$10$...");
        user.setRole(Roles.CUSTOMER);
        user.setEnabled(false);
        String activationToken = UUID.randomUUID().toString();
        user.setActivationToken(activationToken);
        userRepository.save(user);

        boolean activationResult = userService.activateUser(activationToken);

        assertTrue(activationResult, "La activación debería tener éxito con un token válido");
        User activatedUser = userRepository.findByEmail("activateuser@example.com").get();
        assertTrue(activatedUser.isEnabled(), "El usuario debería estar habilitado después de la activación");
        assertNull(activatedUser.getActivationToken(), "El token de activación debería ser nulo después de la activación");

        Account account = accountRepository.findFirstByUserId(activatedUser.getId());
        assertNotNull(account, "La cuenta asociada debería existir");
        assertEquals(AccountStatus.ACTIVE, account.getStatus(), "El estado de la cuenta debería ser ACTIVE después de la activación del usuario");
    }

    @Test
    @DisplayName("Activar usuario con token inválido")
    public void testActivateUser_InvalidToken() {
        String invalidToken = UUID.randomUUID().toString();

        boolean activationResult = userService.activateUser(invalidToken);

        assertFalse(activationResult, "La activación debería fallar con un token inválido");
    }

    @Test
    @DisplayName("Actualizar usuario existente")
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("updateuser@example.com");
        user.setName("Update User");
        user.setPassword("{bcrypt}$2a$10$...");
        user.setPhoneNumber("1112223333");
        user.setRole(Roles.CUSTOMER);
        user.setEnabled(true);
        User savedUser = userService.createUser(user);

        savedUser.setName("Updated Name");
        savedUser.setPhoneNumber("4445556666");

        User updatedUser = userService.updateUser(savedUser);

        assertEquals("Updated Name", updatedUser.getName(), "El nombre del usuario debería haberse actualizado");
        assertEquals("4445556666", updatedUser.getPhoneNumber(), "El número de teléfono debería haberse actualizado");
    }
}
