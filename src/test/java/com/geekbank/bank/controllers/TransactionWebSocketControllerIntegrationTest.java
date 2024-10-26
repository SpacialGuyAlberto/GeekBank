package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionWebSocketController.class)
@ActiveProfiles("test")
@EnableWebSocket
public class TransactionWebSocketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    public void setup() {
        messagingTemplate.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("Enviar transacción por WebSocket")
    public void testSendTransactionStatus() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber("12345");
        transaction.setStatus(TransactionStatus.valueOf("COMPLETED"));

        mockMvc.perform(post("/ws/transaction-status")
                        .contentType("application/json")
                        .content("{\"transactionNumber\":\"12345\", \"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
    }
}
