package com.geekbank.bank.services;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.TransactionRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TelegramListenerIntegrationTest {

    @InjectMocks
    private TelegramListener telegramListener;

    @Mock
    private SmsService smsService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRequestStorageService orderRequestStorageService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionStorageService transactionStorageService;

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Procesar mensaje válido de Telegram")
    public void testProcessValidTelegramMessage() throws Exception {
        // Simular la respuesta de Telegram
        String telegramResponse = new JSONObject()
                .put("ok", true)
                .put("result", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("update_id", 123456)
                                .put("channel_post", new JSONObject()
                                        .put("text", "/Message from 123: Has recibido L 100.00 del 987654321012. Ref. 123456789, Fecha: 12/04/24 10:30 Nuevo balance Tigo Money: L 500.00")
                                        .put("chat", new JSONObject().put("id", 654321))))
                ).toString();

        // Simular el comportamiento de HttpURLConnection
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(telegramResponse.getBytes()));

        // Simular otros servicios
        OrderRequest mockOrderRequest = mock(OrderRequest.class);
        Transaction mockTransaction = mock(Transaction.class);
        when(orderRequestStorageService.getOrderRequestByPhoneNumber(anyString())).thenReturn(mockOrderRequest);
        when(transactionStorageService.findMatchingTransaction(anyString())).thenReturn(mockTransaction);
        when(transactionService.findByTransactionNumber(anyString())).thenReturn(mockTransaction);
        when(mockTransaction.getAmount()).thenReturn(100.0);
        when(mockTransaction.getType()).thenReturn(TransactionType.BALANCE_PURCHASE);

        // Simular la cuenta y el usuario asociado a la transacción
        Account mockAccount = mock(Account.class);
        User mockUser = mock(User.class);
        when(mockTransaction.getUser()).thenReturn(mockUser);
        when(mockUser.getAccount()).thenReturn(mockAccount);

        // Ejecutar el procesamiento del mensaje
        telegramListener.processResponse(telegramResponse);

        // Verificar interacciones
        verify(orderRequestStorageService, times(1)).getOrderRequestByPhoneNumber(anyString());
        verify(transactionStorageService, times(1)).findMatchingTransaction(anyString());
        verify(transactionService, times(1)).updateTransactionStatus(anyLong(), eq(TransactionStatus.COMPLETED), isNull());
    }

    @Test
    @DisplayName("Procesar mensaje no válido de Telegram")
    public void testProcessInvalidTelegramMessage() throws Exception {
        // Simular la respuesta de Telegram con un mensaje que no tiene el formato esperado
        String telegramResponse = new JSONObject()
                .put("ok", true)
                .put("result", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("update_id", 123457)
                                .put("channel_post", new JSONObject()
                                        .put("text", "Mensaje no válido")
                                        .put("chat", new JSONObject().put("id", 654321))))
                ).toString();

        // Simular el comportamiento de HttpURLConnection
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(mockConnection.getInputStream()).thenReturn(new ByteArrayInputStream(telegramResponse.getBytes()));

        // Ejecutar el procesamiento del mensaje
        telegramListener.processResponse(telegramResponse);

        // Verificar que no se llama a los métodos relacionados con la orden o la transacción
        verify(orderRequestStorageService, never()).getOrderRequestByPhoneNumber(anyString());
        verify(transactionStorageService, never()).findMatchingTransaction(anyString());
    }
}
