package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.auth.register.controller.RegisterController;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.support.email.service.SendGridEmailService;
import com.geekbank.bank.user.service.UserService;
import com.geekbank.bank.util.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para RegisterController.
 */
@WebMvcTest(RegisterController.class)
@ActiveProfiles("test")
public class RegisterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SendGridEmailService emailService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/auth/registerUser - Registro de usuario exitoso")
    public void testRegisterUser_Success() throws Exception {
        // Datos de prueba
        RegisterController.RegisterRequest registerRequest = new RegisterController.RegisterRequest();
        registerRequest.setEmail("new.user@example.com");
        registerRequest.setPassword("securePassword");
        registerRequest.setName("New User");

        // Simular que el usuario no existe
        when(userService.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Simular el registro del usuario sin errores
        doNothing().when(userService).registerUser(any(User.class));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/auth/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Usuario registrado correctamente. Por favor, revisa tu email para activar la cuenta."));

        // Verificar que el usuario fue registrado correctamente
        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/registerUser - Usuario ya existe")
    public void testRegisterUser_UserAlreadyExists() throws Exception {
        // Datos de prueba
        RegisterController.RegisterRequest registerRequest = new RegisterController.RegisterRequest();
        registerRequest.setEmail("existing.user@example.com");
        registerRequest.setPassword("securePassword");
        registerRequest.setName("Existing User");

        // Simular que el usuario ya existe
        when(userService.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/auth/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isConflict())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("El usuario ya existe"));

        // Verificar que el usuario no fue registrado
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/registerUserByAdmin - Registro de usuario por admin")
    public void testRegisterUserByAdmin_Success() throws Exception {
        // Datos de prueba
        RegisterController.RegisterRequest registerRequest = new RegisterController.RegisterRequest();
        registerRequest.setEmail("admin.user@example.com");
        registerRequest.setName("Admin User");

        // Simular que el usuario no existe
        when(userService.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        // Simular registro por el admin y el envío de correo
        doNothing().when(userService).registerUserByAdmin(any(User.class));
        doNothing().when(emailService).sendSetPasswordEmail(any(User.class));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/auth/registerUserByAdmin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Usuario registrado correctamente. Se ha enviado un correo electrónico para que el usuario establezca su contraseña."));

        // Verificar que el usuario fue registrado correctamente
        verify(userService, times(1)).registerUserByAdmin(any(User.class));
        verify(emailService, times(1)).sendSetPasswordEmail(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/registerUserByAdmin - Usuario ya existe")
    public void testRegisterUserByAdmin_UserAlreadyExists() throws Exception {
        // Datos de prueba
        RegisterController.RegisterRequest registerRequest = new RegisterController.RegisterRequest();
        registerRequest.setEmail("existing.admin.user@example.com");
        registerRequest.setName("Existing Admin User");

        // Simular que el usuario ya existe
        when(userService.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/auth/registerUserByAdmin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isConflict())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("El usuario ya existe"));

        // Verificar que el usuario no fue registrado
        verify(userService, never()).registerUserByAdmin(any(User.class));
        verify(emailService, never()).sendSetPasswordEmail(any(User.class));
    }

    @Test
    @DisplayName("GET /api/auth/activate - Activación de usuario exitosa")
    public void testActivateUser_Success() throws Exception {
        // Datos de prueba
        String activationToken = UUID.randomUUID().toString();

        // Simular que la activación es exitosa
        when(userService.activateUser(activationToken)).thenReturn(true);

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/auth/activate")
                        .param("token", activationToken))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("User activated successfully."));

        // Verificar que se intentó activar al usuario
        verify(userService, times(1)).activateUser(activationToken);
    }

    @Test
    @DisplayName("GET /api/auth/activate - Token de activación inválido")
    public void testActivateUser_InvalidToken() throws Exception {
        // Datos de prueba
        String invalidToken = UUID.randomUUID().toString();

        // Simular que el token es inválido
        when(userService.activateUser(invalidToken)).thenReturn(false);

        // Realizar la solicitud GET
        mockMvc.perform(get("/api/auth/activate")
                        .param("token", invalidToken))
                // Verificar el estado de la respuesta
                .andExpect(status().isBadRequest())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Invalid activation token."));

        // Verificar que se intentó activar al usuario
        verify(userService, times(1)).activateUser(invalidToken);
    }
}
