package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.BalancePurchaseRequest;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionType;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@ActiveProfiles("test")
public class BalanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionService transactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/balance/purchase - Realizar una compra de balance (Usuario existente)")
    public void testPurchaseBalance_UserExists() throws Exception {
        // Datos de prueba
        BalancePurchaseRequest balanceRequest = new BalancePurchaseRequest();
        balanceRequest.setUserId(1L);
        balanceRequest.setGuestId(null);
        balanceRequest.setAmount(100.00);
        balanceRequest.setPhoneNumber("123456789");

        User user = new User();
        user.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setTransactionNumber("TRX123");

        // Simular búsqueda del usuario en el repositorio
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simular creación de la transacción en el servicio
        when(transactionService.createTransaction(
                any(User.class),
                isNull(),
                isNull(),
                anyString(),
                anyDouble(),
                any(TransactionType.class),
                anyString(),
                anyString(),
                eq(Collections.emptyList()), // Lista vacía de productos
                anyBoolean()
        )).thenReturn(transaction);

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/balance/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(balanceRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isOk())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("Balance purchase initiated. Transaction number: TRX123"));
    }

    @Test
    @DisplayName("POST /api/balance/purchase - Realizar una compra de balance (Usuario no encontrado)")
    public void testPurchaseBalance_UserNotFound() throws Exception {
        // Datos de prueba
        BalancePurchaseRequest balanceRequest = new BalancePurchaseRequest();
        balanceRequest.setUserId(2L);
        balanceRequest.setGuestId(null);
        balanceRequest.setAmount(50.00);
        balanceRequest.setPhoneNumber("987654321");

        // Simular que el usuario no fue encontrado en el repositorio
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/balance/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(balanceRequest)))
                // Verificar el estado de la respuesta
                .andExpect(status().isNotFound())
                // Verificar el contenido de la respuesta
                .andExpect(content().string("User not found"));
    }

    @Test
    @DisplayName("POST /api/balance/purchase - Error al crear la transacción")
    public void testPurchaseBalance_TransactionCreationError() throws Exception {
        // Datos de prueba
        BalancePurchaseRequest balanceRequest = new BalancePurchaseRequest();
        balanceRequest.setUserId(1L);
        balanceRequest.setGuestId(null);
        balanceRequest.setAmount(100.00);
        balanceRequest.setPhoneNumber("123456789");

        User user = new User();
        user.setId(1L);

        // Simular que el usuario fue encontrado
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Simular que se produce una excepción al crear la transacción
        when(transactionService.createTransaction(
                any(User.class),
                isNull(),
                isNull(),
                anyString(),
                anyDouble(),
                any(TransactionType.class),
                anyString(),
                anyString(),
                eq(Collections.emptyList()),
                anyBoolean()
        )).thenThrow(new RuntimeException("Error de creación de transacción"));

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/balance/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(balanceRequest)))
                // Verificar el estado de la respuesta y el mensaje de error
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error creating transaction: Error de creación de transacción"));
    }
}
