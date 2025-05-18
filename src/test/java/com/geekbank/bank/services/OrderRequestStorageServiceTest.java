package com.geekbank.bank.services;

import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.order.service.OrderRequestStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "SPRING_APPLICATION_NAME=geekbank-test")
@ActiveProfiles("test")
public class OrderRequestStorageServiceTest {

    @InjectMocks
    private OrderRequestStorageService orderRequestStorageService;

    private OrderRequest testOrderRequest;

    @BeforeEach
    public void setUp() {
        testOrderRequest = new OrderRequest();
        testOrderRequest.setPhoneNumber("123456789");
        testOrderRequest.setOrderRequestId("req123");
        testOrderRequest.setCreatedAt(LocalDateTime.now());
        orderRequestStorageService.storeOrderRequest(testOrderRequest);
    }

    @Test
    @DisplayName("Almacenar y recuperar una solicitud de pedido por número de teléfono")
    public void testStoreAndRetrieveOrderRequest() {
        OrderRequest retrievedOrder = orderRequestStorageService.getOrderRequestByPhoneNumber("123456789");

        assertNotNull(retrievedOrder, "Debería haberse recuperado una solicitud de pedido");
        assertEquals("123456789", retrievedOrder.getPhoneNumber(), "El número de teléfono debería coincidir");
    }

    @Test
    @DisplayName("Verificar existencia de solicitud de pedido por número de teléfono")
    public void testHasOrderForPhoneNumber() {
        boolean hasOrder = orderRequestStorageService.hasOrderForPhoneNumber("123456789");

        assertTrue(hasOrder, "Debería existir una solicitud de pedido para este número de teléfono");
    }

    @Test
    @DisplayName("Eliminar una solicitud de pedido por número de teléfono")
    public void testRemoveOrderRequest() {
        orderRequestStorageService.removeOrderRequest("123456789");
        OrderRequest removedOrder = orderRequestStorageService.getOrderRequestByPhoneNumber("123456789");

        assertNull(removedOrder, "La solicitud de pedido debería haberse eliminado");
    }

    @Test
    @DisplayName("Eliminar una solicitud de pedido por ID")
    public void testRemoveOrderRequestById() {
        orderRequestStorageService.removeOrderRequestById("req123");
        OrderRequest removedOrder = orderRequestStorageService.getOrderRequestByPhoneNumber("123456789");

        assertNull(removedOrder, "La solicitud de pedido debería haberse eliminado por ID");
    }

    @Test
    @DisplayName("Expirar solicitudes de pedido antiguas")
    public void testExpireOrderRequests() {
        // Simular que la solicitud de pedido fue creada hace más de 5 minutos
        testOrderRequest.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        orderRequestStorageService.storeOrderRequest(testOrderRequest);

        // Ejecutar el método programado para expirar pedidos
        orderRequestStorageService.expireOrderRequests();

        OrderRequest expiredOrder = orderRequestStorageService.getOrderRequestByPhoneNumber("123456789");
        assertNull(expiredOrder, "La solicitud de pedido debería haberse expirado y eliminado");
    }
}
