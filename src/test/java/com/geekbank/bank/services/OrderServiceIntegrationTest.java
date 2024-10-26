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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest(properties = "SPRING_APPLICATION_NAME=geekbank-test")
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private KinguinService kinguinService;

    @Mock
    private SmsService smsService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Realizar pedido exitoso")
    public void testPlaceOrder_Success() {
        // Preparar datos de prueba
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setPhoneNumber("123456789");

        OrderResponse mockResponse = new OrderResponse();
        mockResponse.setOrderId("12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(OrderResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        OrderResponse response = orderService.placeOrder(orderRequest);

        assertNotNull(response, "La respuesta no debería ser nula");
        assertEquals("12345", response.getOrderId(), "El ID de la orden debería ser '12345'");

        // Verificar que `smsService` no se llama hasta que se complete el pedido
        verify(smsService, never()).sendKeysToPhoneNumber(anyString(), anyList());
    }

    @Test
    @DisplayName("Descargar claves de una orden")
    public void testDownloadKeys() {
        String orderId = "12345";
        List<String> mockKeys = Arrays.asList("key1", "key2", "key3");

        List<Map<String, String>> responseBody = Arrays.asList(
                Map.of("serial", "key1"),
                Map.of("serial", "key2"),
                Map.of("serial", "key3")
        );

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        List<String> keys = orderService.downloadKeys(orderId);

        assertEquals(3, keys.size(), "Deberían haber tres claves en la lista");
        assertEquals(mockKeys, keys, "Las claves descargadas deberían coincidir con las simuladas");
    }

    @Test
    @DisplayName("Obtener número de teléfono por ID de orden")
    public void testGetPhoneNumberByOrderId() {
        String orderId = "12345";
        String expectedPhoneNumber = "123456789";

        String phoneNumber = orderService.getPhoneNumberByOrderId(orderId);

        assertNotNull(phoneNumber, "El número de teléfono no debería ser nulo");
        assertEquals("El número de teléfono asociado a la orden", phoneNumber, "El número de teléfono debería coincidir con el valor esperado");
    }
}
