package com.geekbank.bank.controllers;

import com.geekbank.bank.support.sms.controller.SmsController;
import com.geekbank.bank.support.sms.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmsController.class)
@ActiveProfiles("test")
public class SmsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SmsService smsService;

    @Test
    @DisplayName("POST /api/sms/send - Enviar SMS a un número específico")
    public void testSendSms() throws Exception {
        String testPhoneNumber = "+123456789";

        // Realizar la solicitud POST para enviar SMS
        mockMvc.perform(post("/api/sms/send")
                        .param("toPhoneNumber", testPhoneNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("SMS sent to " + testPhoneNumber));

        // Verificar que el servicio SMS fue llamado con el número correcto
        verify(smsService, times(1)).sendPaymentNotification(testPhoneNumber);
    }
}
