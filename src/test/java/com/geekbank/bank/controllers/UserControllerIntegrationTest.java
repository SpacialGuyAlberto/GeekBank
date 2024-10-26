package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.AccountService;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.util.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para UserController.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @MockBean
    private PasswordEncoder passwordEncoder;

    // @MockBean
    // private com.geekbank.bank.repositories.UserRepository userRepository;

    /**
     * Método auxiliar para crear un usuario de prueba.
     */
    private User createTestUser(Long id, String name, String email, String phoneNumber) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword("{bcrypt}$2a$10$abcdefghijklmnopqrstuv"); // Contraseña encriptada de ejemplo
        user.setRole(com.geekbank.bank.models.Roles.CUSTOMER);
        user.setEnabled(true);
        return user;
    }

    @Test
    @DisplayName("GET /api/users/{userId} - Obtener usuario por ID (Existente)")
    public void testGetUserById_Existing() throws Exception {
        // Datos de prueba
        Long userId = 1L;
        User testUser = createTestUser(userId, "John Doe", "john.doe@example.com", "1234567890");

        // Simular comportamiento del servicio
        when(userService.getUserById(userId)).thenReturn(Optional.of(testUser));

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{userId} - Obtener usuario por ID (No Existente)")
    public void testGetUserById_NotExisting() throws Exception {
        // Datos de prueba
        Long userId = 2L;

        // Simular comportamiento del servicio
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isNotFound());

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/users/checkUser - Verificar existencia de usuario por email (Existe)")
    public void testCheckIfUserExists_Exists() throws Exception {
        // Datos de prueba
        String email = "jane.doe@example.com";
        User existingUser = createTestUser(3L, "Jane Doe", email, "0987654321");

        // Simular comportamiento del servicio
        when(userService.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/checkUser")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.exists", is(true)));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("GET /api/users/checkUser - Verificar existencia de usuario por email (No Existe)")
    public void testCheckIfUserExists_NotExists() throws Exception {
        // Datos de prueba
        String email = "nonexistent@example.com";

        // Simular comportamiento del servicio
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users/checkUser")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.exists", is(false)));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("GET /api/users - Obtener todos los usuarios")
    public void testGetAllUsers() throws Exception {
        // Datos de prueba
        User user1 = createTestUser(4L, "Alice Smith", "alice.smith@example.com", "1112223333");
        User user2 = createTestUser(5L, "Bob Johnson", "bob.johnson@example.com", "4445556666");
        List<User> users = Arrays.asList(user1, user2);

        // Simular comportamiento del servicio
        when(userService.findAllUsers()).thenReturn(users);

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.length()", is(users.size())))
                .andExpect(jsonPath("$[0].id", is(user1.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Alice Smith")))
                .andExpect(jsonPath("$[1].id", is(user2.getId().intValue())))
                .andExpect(jsonPath("$[1].name", is("Bob Johnson")));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("POST /api/users - Registrar un nuevo usuario")
    public void testRegisterUser() throws Exception {
        // Datos de prueba
        User newUser = new User();
        newUser.setName("Charlie Brown");
        newUser.setEmail("charlie.brown@example.com");
        newUser.setPassword("password123"); // Contraseña en texto plano (debería ser encriptada en el servicio)
        newUser.setPhoneNumber("7778889999");
        newUser.setRole(com.geekbank.bank.models.Roles.CUSTOMER);
        newUser.setEnabled(false); // Usuario no habilitado inicialmente

        // Simular comportamiento del servicio
        doNothing().when(userService).registerUser(any(User.class));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newUser)))
                // Verificar el estado de la respuesta
                .andExpect(status().isCreated())
                // Verificar el encabezado Location
                .andExpect(header().string("Location", containsString("/api/users/")))
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.name", is("Charlie Brown")))
                .andExpect(jsonPath("$.email", is("charlie.brown@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("7778889999")))
                .andExpect(jsonPath("$.enabled", is(false)));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("GET /api/users/user-details - Obtener detalles del usuario autenticado (Autenticado)")
    public void testGetUserDetails_Authenticated() throws Exception {
        // Datos de prueba
        String email = "authenticated.user@example.com";
        User authenticatedUser = createTestUser(6L, "David Lee", email, "0001112222");

        // Simular autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);

        // Simular comportamiento del servicio
        when(userService.findByEmail(email)).thenReturn(Optional.of(authenticatedUser));

        // Realizar la solicitud GET con autenticación simulada
        mockMvc.perform(get("/api/users/user-details")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.id", is(authenticatedUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is("David Lee")))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.phoneNumber", is("0001112222")));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("GET /api/users/user-details - Obtener detalles del usuario autenticado (No Autenticado)")
    public void testGetUserDetails_NotAuthenticated() throws Exception {
        // Realizar la solicitud GET sin autenticación
        mockMvc.perform(get("/api/users/user-details")
                        .contentType(MediaType.APPLICATION_JSON))
                // Verificar el estado de la respuesta
                .andExpect(status().isUnauthorized());

        // Verificar que el servicio no fue llamado
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("POST /api/users/update-user-details - Actualizar detalles del usuario (Autenticado y Exitoso)")
    public void testUpdateUserDetails_Success() throws Exception {
        // Datos de prueba que representan la solicitud enviada desde el frontend
        String email = "update.user@example.com";
        String password = "securePassword";
        String newEmail = "updated.user@example.com";
        String newName = "Updated User";
        String newPhoneNumber = "3334445555";

        // Crear el JSON de la solicitud que simula los datos recibidos del frontend
        String userInfoRequestJson = new ObjectMapper().writeValueAsString(
                Map.of(
                        "email", newEmail,
                        "password", password,
                        "name", newName,
                        "phoneNumber", newPhoneNumber
                )
        );

        // Simular el objeto de autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);

        Authentication authResponse = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResponse);

        // Simular detalles del usuario autenticado
        UserDetails userDetails = mock(UserDetails.class);
        when(authResponse.getPrincipal()).thenReturn(userDetails);

        // Configurar el usuario existente para la actualización
        User existingUser = createTestUser(7L, "Old Name", email, "6667778888");
        when(userService.findByEmail(newEmail)).thenReturn(Optional.of(existingUser));

        // Simular generación del token JWT
        String jwtToken = "mocked-jwt-token";
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn(jwtToken);

        // Realizar la solicitud POST utilizando el JSON creado
        mockMvc.perform(post("/api/users/update-user-details")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInfoRequestJson))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.token", is(jwtToken)))
                .andExpect(jsonPath("$.userId", is(existingUser.getId().toString())));

        // Verificar que los métodos del servicio fueron llamados correctamente
        verify(userService, times(1)).findByEmail(newEmail);
        verify(userService, times(1)).updateUser(existingUser);

        // Verificar que el token fue generado correctamente
        verify(jwtTokenUtil, times(1)).generateToken(userDetails);
    }



    @Test
    @DisplayName("POST /api/users/update-user-details - Actualizar detalles del usuario (Autenticación Fallida)")
    public void testUpdateUserDetails_AuthenticationFailed() throws Exception {
        // Datos de prueba que representarían la solicitud enviada desde el frontend
        String email = "fail.auth.user@example.com";
        String password = "wrongPassword";
        String newEmail = "updated.user@example.com";
        String newName = "Updated User";
        String newPhoneNumber = "9998887777";

        // Crear el JSON de la solicitud simulando los datos del frontend
        String userInfoRequestJson = new ObjectMapper().writeValueAsString(
                Map.of(
                        "email", newEmail,
                        "password", password,
                        "name", newName,
                        "phoneNumber", newPhoneNumber
                )
        );

        // Simular el objeto de autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);

        // Simular que la autenticación manager lanzará una excepción para fallar la autenticación
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Autenticación fallida") {});

        // Realizar la solicitud POST usando el JSON creado
        mockMvc.perform(post("/api/users/update-user-details")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInfoRequestJson))
                // Verificar el estado de la respuesta
                .andExpect(status().isUnauthorized())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.error", containsString("Error de autenticación")));

        // Verificar que el método de actualización de usuario no fue llamado
        verify(userService, never()).updateUser(any(User.class));
    }


    @Test
    @DisplayName("POST /api/users/setPassword - Establecer contraseña (Token Válido)")
    public void testSetPassword_ValidToken() throws Exception {
        // Datos de prueba
        String token = "valid-token-123";
        String newPassword = "newSecurePassword";

        // Objeto de solicitud
        UserController.SetPasswordRequest request = new UserController.SetPasswordRequest();
        request.setToken(token);
        request.setPassword(newPassword);

        // Simular búsqueda del usuario por token
        // Dado que UserRepository ya no está mockeado, necesitamos ajustar esto.
        // En su lugar, deberías mockear el UserService o crear una configuración diferente para pruebas que involucren repositorios.
        // Aquí, asumiremos que UserService maneja la lógica de encontrar y actualizar usuarios por token.

        User user = createTestUser(8L, "Eve Adams", "eve.adams@example.com", "5556667777");
        when(userService.findByActivationToken(token)).thenReturn(Optional.of(user));

        // Simular servicios relacionados
        com.geekbank.bank.models.Account account = mock(com.geekbank.bank.models.Account.class);
        when(accountService.getAccountsByUserId(user.getId())).thenReturn(account);

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/users/setPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Contraseña establecida correctamente."));

        // Verificar que el usuario haya sido actualizado
        verify(userService, times(1)).findByActivationToken(token);
        verify(accountService, times(1)).getAccountsByUserId(user.getId());
        verify(accountService, times(1)).changeVerificationStatus(account.getId(), com.geekbank.bank.models.VerificationStatus.VERIFIED);
        verify(userService, times(1)).saveUser(user); // Asumiendo que tienes un método saveUser en UserService
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()), "La contraseña debería haberse actualizado y encriptado");
        assertTrue(user.isEnabled(), "El usuario debería estar habilitado");
        assertNull(user.getActivationToken(), "El token de activación debería haber sido eliminado");
    }

    @Test
    @DisplayName("POST /api/users/setPassword - Establecer contraseña (Token Inválido)")
    public void testSetPassword_InvalidToken() throws Exception {
        // Datos de prueba
        String token = "invalid-token-456";
        String newPassword = "anotherPassword";

        // Objeto de solicitud
        UserController.SetPasswordRequest request = new UserController.SetPasswordRequest();
        request.setToken(token);
        request.setPassword(newPassword);

        // Simular búsqueda del usuario por token (no encontrado)
        when(userService.findByActivationToken(token)).thenReturn(Optional.empty());

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/users/setPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                // Verificar el estado de la respuesta
                .andExpect(status().isBadRequest())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Token inválido o expirado."));

        // Verificar que el usuario no haya sido actualizado
        verify(userService, times(1)).findByActivationToken(token);
        verify(accountService, never()).getAccountsByUserId(anyLong());
        verify(userService, never()).saveUser(any(User.class)); // Asumiendo que tienes un método saveUser en UserService
    }

    @Test
    @DisplayName("POST /api/users/update-user-details - Actualizar detalles del usuario (Usuario No Habilitado)")
    public void testUpdateUserDetails_UserNotEnabled() throws Exception {
        // Datos de prueba que representarían la solicitud enviada desde el frontend
        String email = "disabled.user@example.com";
        String password = "securePassword";
        String newEmail = "updated.disabled.user@example.com";
        String newName = "Updated Disabled User";
        String newPhoneNumber = "3334445555";

        // Crear el JSON de la solicitud simulando los datos del frontend
        String userInfoRequestJson = new ObjectMapper().writeValueAsString(
                Map.of(
                        "email", newEmail,
                        "password", password,
                        "name", newName,
                        "phoneNumber", newPhoneNumber
                )
        );

        // Simular el objeto de autenticación
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);

        // Simular autenticación manager
        Authentication authResponse = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResponse);

        // Simular detalles del usuario
        UserDetails userDetails = mock(UserDetails.class);
        when(authResponse.getPrincipal()).thenReturn(userDetails);

        // Simular búsqueda del usuario y su estado deshabilitado
        User existingUser = createTestUser(9L, "Frank Miller", email, "2223334444");
        existingUser.setEnabled(false); // Usuario no habilitado
        when(userService.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Realizar la solicitud POST usando el JSON creado
        mockMvc.perform(post("/api/users/update-user-details")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInfoRequestJson))
                // Verificar el estado de la respuesta
                .andExpect(status().isForbidden())
                // Verificar el contenido de la respuesta
                .andExpect(jsonPath("$.error", is("Usuario no habilitado")));

        // Verificar que el usuario no haya sido actualizado y no se generó el token
        verify(userService, times(1)).findByEmail(email);
        verify(userService, never()).updateUser(any(User.class));
        verify(jwtTokenUtil, never()).generateToken(any(UserDetails.class));
    }


    // Agrega más pruebas según sea necesario para cubrir otros casos de uso

}
