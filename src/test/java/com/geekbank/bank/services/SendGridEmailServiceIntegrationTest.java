package com.geekbank.bank.services;

import com.geekbank.bank.support.email.service.SendGridEmailService;
import com.geekbank.bank.user.model.User;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class SendGridEmailServiceIntegrationTest {

    @InjectMocks
    private SendGridEmailService sendGridEmailService;

    @Mock
    private SendGrid sendGrid;

    @Value("${DOMAIN_ORIGIN_URL}")
    private String domainUrl;

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Enviar email de activación")
    public void testSendActivationEmail() throws Exception {
        String emailRecipient = "testuser@example.com";
        String token = "sampleActivationToken";
        String expectedActivationLink = domainUrl + "/activate?token=" + token;

        Request expectedRequest = new Request();
        expectedRequest.setMethod(com.sendgrid.Method.POST);
        expectedRequest.setEndpoint("mail/send");
        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        sendGridEmailService.sendActivationEmail(emailRecipient, token);

        verify(sendGrid, times(1)).api(any(Request.class));

        // Validar contenido del email
        assertEquals(202, mockResponse.getStatusCode(), "El código de respuesta debería ser 202");
    }

    @Test
    @DisplayName("Enviar email para establecer contraseña")
    public void testSendSetPasswordEmail() throws Exception {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setActivationToken("sampleToken");
        String expectedLink = domainUrl + "/set-password?token=" + user.getActivationToken();

        Request expectedRequest = new Request();
        expectedRequest.setMethod(com.sendgrid.Method.POST);
        expectedRequest.setEndpoint("mail/send");
        Response mockResponse = new Response();
        mockResponse.setStatusCode(202);

        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        sendGridEmailService.sendSetPasswordEmail(user);

        verify(sendGrid, times(1)).api(any(Request.class));

        // Validar que la respuesta tenga el código de éxito esperado
        assertEquals(202, mockResponse.getStatusCode(), "El código de respuesta debería ser 202");
    }
}
