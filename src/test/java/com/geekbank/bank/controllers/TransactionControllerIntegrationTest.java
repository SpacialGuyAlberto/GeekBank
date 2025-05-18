package com.geekbank.bank.controllers;

import com.geekbank.bank.transaction.controller.TransactionController;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.transaction.constants.TransactionStatus;
import com.geekbank.bank.transaction.repository.TransactionRepository;
import com.geekbank.bank.order.service.OrderRequestStorageService;
import com.geekbank.bank.transaction.service.TransactionService;
import com.geekbank.bank.transaction.service.TransactionStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionStorageService transactionStorageService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private OrderRequestStorageService orderRequestStorageService;

    @Test
    @DisplayName("GET /api/transactions/pending - Obtener transacciones pendientes por número de teléfono")
    public void testGetPendingTransactionsByPhoneNumber() throws Exception {
        String phoneNumber = "1234567890";
        List<Transaction> transactions = List.of(new Transaction());

        when(transactionService.findPendingTransactionsByPhoneNumber(phoneNumber)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/pending")
                        .param("phoneNumber", phoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).findPendingTransactionsByPhoneNumber(phoneNumber);
    }

    @Test
    @DisplayName("GET /api/transactions - Obtener todas las transacciones")
    public void testGetAllTransactions() throws Exception {
        List<Transaction> transactions = List.of(new Transaction());

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    @DisplayName("GET /api/transactions/{userId} - Obtener transacciones por ID de usuario")
    public void testGetTransactionsByUserId() throws Exception {
        long userId = 1L;
        List<Transaction> transactions = List.of(new Transaction());

        when(transactionService.getTransactionByUserId(userId)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getTransactionByUserId(userId);
    }

    @Test
    @DisplayName("GET /api/transactions/filter - Obtener transacciones por usuario y rango de fechas")
    public void testGetTransactionsByUserIdAndTimestamp() throws Exception {
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = List.of(new Transaction());
        when(transactionService.getTransactionsByUserIdAndTimestamp(userId, start, end)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/filter")
                        .param("userId", String.valueOf(userId))
                        .param("start", start.toString())
                        .param("end", end.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getTransactionsByUserIdAndTimestamp(userId, start, end);
    }

    @Test
    @DisplayName("PUT /api/transactions/cancel/{transactionId}/{orderRequestId} - Cancelar transacción")
    public void testCancelRunningTransaction() throws Exception {
        String transactionId = "12345";
        String orderRequestId = "54321";
        Transaction transaction = new Transaction();
        transaction.setId(1L);

        when(transactionService.findByTransactionNumber(transactionId)).thenReturn(transaction);

        mockMvc.perform(put("/api/transactions/cancel/{transactionId}/{orderRequestId}", transactionId, orderRequestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionStorageService, times(1)).removeTransactionById(transaction.getId());
        verify(transactionService, times(1)).updateTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED, "User Canceled");
        verify(transactionRepository, times(1)).save(transaction);
        verify(orderRequestStorageService, times(1)).removeOrderRequestById(orderRequestId);
    }

    @Test
    @DisplayName("GET /api/transactions/by-number/{transactionNumber} - Obtener transacción por número de transacción")
    public void testGetTransactionByNumber() throws Exception {
        String transactionNumber = "12345";
        Transaction transaction = new Transaction();

        when(transactionService.findByTransactionNumber(transactionNumber)).thenReturn(transaction);

        mockMvc.perform(get("/api/transactions/by-number/{transactionNumber}", transactionNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).findByTransactionNumber(transactionNumber);
    }
}
