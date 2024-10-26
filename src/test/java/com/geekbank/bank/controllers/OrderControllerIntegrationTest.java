package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TigoService tigoService;

    @MockBean
    private UserService userService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private OrderRequestStorageService orderRequestStorageService;

    @MockBean
    private SmsService smsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/orders - Colocar orden de compra exitosa")
    public void testPlaceOrder_Success() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setAmount(100.0);
        orderRequest.setPhoneNumber("1234567890");
        orderRequest.setOrderRequestId();

        User user = new User();
        user.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setTransactionNumber("TRANS123");

        when(userRepository.findById(orderRequest.getUserId())).thenReturn(Optional.of(user));
        when(transactionService.createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any()))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Order placed successfully: " + orderRequest.getOrderRequestId() + "\n Transaction number: " + transaction.getTransactionNumber()));

        verify(orderRequestStorageService, times(1)).storeOrderRequest(orderRequest);
        verify(transactionService, times(1)).createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/orders - Error al crear transacción debido a argumentos inválidos")
    public void testPlaceOrder_TransactionCreationError() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setAmount(100.0);
        orderRequest.setPhoneNumber("1234567890");

        when(userRepository.findById(orderRequest.getUserId())).thenReturn(Optional.of(new User()));
        when(transactionService.createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Cantidad inválida"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cantidad inválida"));

        verify(transactionService, times(1)).createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/orders - Error en servidor al crear transacción")
    public void testPlaceOrder_ServerErrorOnTransaction() throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(1L);
        orderRequest.setAmount(100.0);
        orderRequest.setPhoneNumber("1234567890");

        when(userRepository.findById(orderRequest.getUserId())).thenReturn(Optional.of(new User()));
        when(transactionService.createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any()))
                .thenThrow(new RuntimeException("Error en el servidor"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al crear la transacción: Error en el servidor"));

        verify(transactionService, times(1)).createTransaction(any(User.class), any(), any(), anyDouble(), any(TransactionType.class), any(), any(), any());
    }
}
