package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.user.account.controller.AccountController;
import com.geekbank.bank.user.account.service.AccountService;
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
import java.util.Collections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para AccountController.
 */
@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/accounts - Obtener todas las cuentas paginadas")
    public void testGetAllAccounts() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");

        Page<Account> accountPage = new PageImpl<>(Collections.singletonList(account));

        when(accountService.getAllAccounts(any(PageRequest.class))).thenReturn(accountPage);

        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountNumber").value("123456"));

        verify(accountService, times(1)).getAllAccounts(any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - Obtener cuenta por ID (Existente)")
    public void testGetAccountById_Existing() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("123456");

        when(accountService.getAccountById(1L)).thenReturn(Optional.of(account));

        mockMvc.perform(get("/api/accounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123456"));

        verify(accountService, times(1)).getAccountById(1L);
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - Obtener cuenta por ID (No Existente)")
    public void testGetAccountById_NotExisting() throws Exception {
        when(accountService.getAccountById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountById(2L);
    }

    @Test
    @DisplayName("POST /api/accounts - Crear una nueva cuenta")
    public void testCreateAccount() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");

        when(accountService.createAccount(any(Account.class))).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("123456"));

        verify(accountService, times(1)).createAccount(any(Account.class));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} - Eliminar cuenta por ID")
    public void testDeleteAccount() throws Exception {
        doNothing().when(accountService).deleteAccount(anyLong());

        mockMvc.perform(delete("/api/accounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).deleteAccount(1L);
    }
}
