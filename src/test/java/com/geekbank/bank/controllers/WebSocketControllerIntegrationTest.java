package com.geekbank.bank.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WebSocketControllerIntegrationTest {

    @Autowired
    private WebSocketController webSocketController;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Captor
    private ArgumentCaptor<Map<String, String>> payloadCaptor;

    @Test
    @DisplayName("Notify transaction update via WebSocket")
    public void testNotifyTransactionUpdate() {
        String phoneNumber = "123456789";
        String status = "COMPLETED";
        String failureReason = "N/A";
        String transactionId ="dasads";

        // Invocar el método a probar
        webSocketController.notifyTransactionStatus(phoneNumber, status, failureReason, transactionId);

        // Verificar que `convertAndSend` fue llamado con los parámetros correctos
        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/transaction-status/" + phoneNumber), payloadCaptor.capture());

        Map<String, String> capturedPayload = payloadCaptor.getValue();

        assertEquals(status, capturedPayload.get("status"));
        assertEquals(failureReason, capturedPayload.get("reason"));
    }
}
