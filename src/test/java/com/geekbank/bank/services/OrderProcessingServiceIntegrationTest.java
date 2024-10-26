package com.geekbank.bank.services;

import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderProcessingServiceIntegrationTest {

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    @Mock
    private OrderService orderService;

    @Mock
    private SmsService smsService;

    @Mock
    private OrderRequestStorageService orderRequestStorageService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Procesar orden desde mensaje de Telegram con éxito")
    public void testProcessOrderFromTelegramMessage_Success() {
        // Simular el mensaje de Telegram
        String telegramMessage = "some valid message with phone and order details";

        // Crear OrderRequest temporal y simular que existe en almacenamiento
        OrderRequest mockOrderRequest = new OrderRequest();
        mockOrderRequest.setPhoneNumber("123456789");
        when(orderRequestStorageService.getOrderRequestByPhoneNumber("123456789")).thenReturn(mockOrderRequest);

        // Simular respuesta exitosa de la orden
        OrderResponse mockOrderResponse = new OrderResponse();
        mockOrderResponse.setOrderId("12345");
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(mockOrderResponse);

        // Ejecutar método de procesamiento
        orderProcessingService.processOrderFromTelegramMessage(telegramMessage);

        // Verificar interacciones
        verify(smsService).sendPaymentNotification("123456789");
        verify(orderRequestStorageService).removeOrderRequest("123456789");
        assertNotNull(mockOrderResponse.getOrderId(), "La orden debe ser procesada y tener un ID");
    }

    @Test
    @DisplayName("Procesar orden con mensaje de Telegram inválido")
    public void testProcessOrderFromTelegramMessage_InvalidMessage() {
        // Mensaje de Telegram inválido
        String invalidTelegramMessage = "invalid message without order details";

        // Ejecutar método de procesamiento con mensaje inválido
        orderProcessingService.processOrderFromTelegramMessage(invalidTelegramMessage);

        // Verificar que no hubo interacción con otros servicios
        verify(orderRequestStorageService, never()).getOrderRequestByPhoneNumber(anyString());
        verify(orderService, never()).placeOrder(any(OrderRequest.class));
        verify(smsService, never()).sendPaymentNotification(anyString());
    }
}
