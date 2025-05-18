package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.auth.login.controller.LoginController;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.UserDetailsImpl;
import com.geekbank.bank.user.service.UserService;
import com.geekbank.bank.util.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@ActiveProfiles("test")
public class LoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/auth/login - Login exitoso")
    public void testLogin_Success() throws Exception {
        String email = "test.user@example.com";
        String password = "securePassword";
        String token = "jwtToken";

        LoginController.LoginRequest loginRequest = new LoginController.LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new UserDetailsImpl(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenUtil.generateToken(any(UserDetailsImpl.class))).thenReturn(token);
        when(userService.findByEmail(email)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value("1"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Login fallido (Credenciales incorrectas)")
    public void testLogin_BadCredentials() throws Exception {
        String email = "wrong.user@example.com";
        String password = "wrongPassword";

        LoginController.LoginRequest loginRequest = new LoginController.LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Restablecimiento de contraseña exitoso")
    public void testResetPassword_Success() throws Exception {
        String email = "test.user@example.com";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String token = "jwtToken";

        LoginController.ResetPasswordRequest resetPasswordRequest = new LoginController.ResetPasswordRequest();
        resetPasswordRequest.setEmail(email);
        resetPasswordRequest.setOldPassword(oldPassword);
        resetPasswordRequest.setNewPassword(newPassword);

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(oldPassword));
        user.setEnabled(true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new UserDetailsImpl(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
        when(jwtTokenUtil.generateToken(any(UserDetailsImpl.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.userId").value("1"));

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - Restablecimiento de contraseña fallido (Credenciales incorrectas)")
    public void testResetPassword_BadCredentials() throws Exception {
        String email = "test.user@example.com";
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";

        LoginController.ResetPasswordRequest resetPasswordRequest = new LoginController.ResetPasswordRequest();
        resetPasswordRequest.setEmail(email);
        resetPasswordRequest.setOldPassword(oldPassword);
        resetPasswordRequest.setNewPassword(newPassword);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));

        verify(userService, never()).updateUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/google-login - Login con Google exitoso")
    public void testGoogleLogin_Success() throws Exception {
        String googleToken = "googleToken";
        String email = "google.user@example.com";
        String jwtToken = "jwtToken";

        Jwt decodedJwt = mock(Jwt.class);
        when(decodedJwt.getClaim("email")).thenReturn(email);
        when(decodedJwt.getClaim("name")).thenReturn("Google User");

        when(jwtDecoder.decode(googleToken)).thenReturn(decodedJwt);

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenUtil.generateToken(any(UserDetailsImpl.class))).thenReturn(jwtToken);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("token", googleToken);

        mockMvc.perform(post("/api/auth/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken))
                .andExpect(jsonPath("$.userId").value("1"));

        verify(userService, times(1)).findByEmail(email);
    }
}
