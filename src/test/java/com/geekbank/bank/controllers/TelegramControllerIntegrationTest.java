package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.payment.tigo.utilities.telegram.controller.TelegramController;
import com.geekbank.bank.support.sms.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TelegramController.class)
@ActiveProfiles("test")
public class TelegramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmsService smsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/telegram/update - Procesar mensaje de actualización de Telegram")
    public void testHandleUpdate() throws Exception {
        // Crear una solicitud de prueba con un mensaje simulado
        TelegramController.UpdateRequest updateRequest = new TelegramController.UpdateRequest();
        updateRequest.setMessage("Payment of $20.00 to +123456789");

        // Convertir la solicitud a JSON
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        // Realizar la solicitud POST
        mockMvc.perform(post("/api/telegram/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk());

        // Verificar que el servicio SMS fue llamado
        verify(smsService, times(1)).sendPaymentNotification(anyString());
    }
}
