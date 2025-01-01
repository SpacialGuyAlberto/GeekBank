package com.geekbank.bank.services;

import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.OrdersRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private KinguinService kinguinService; // si lo usas

    @Autowired
    private SendGridEmailService sendGridEmailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    // *** Inyectamos el nuevo servicio ***
    @Autowired
    private KeyPollingService keyPollingService;

    // *** Constantes o endpoints de tu API ***
    private static final String KINGUIN_ORDER_URL = "https://gateway.kinguin.net/esa/api/v1/order";
    private static final String API_KEY = "77d96c852356b1c654a80f424d67048f";

    public Orders createOrder(OrderRequest orderRequest, Transaction transaction){
        Orders order = new Orders();
        order.setTransaction(transaction);
        order.setUserId(orderRequest.getUserId());
        order.setGameUserId(orderRequest.getGameUserId());
        order.setProducts(transaction.getProducts());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setGuestId(orderRequest.getGuestId());
        order.setRefNumber(orderRequest.getRefNumber());
        order.setAmount(orderRequest.getAmount());
        order.setManual(orderRequest.getManual());
        order.setCreatedAt(LocalDateTime.now());

        return ordersRepository.save(order);
    }

    public OrderResponse placeOrder(OrderRequest orderRequest, Transaction transaction) {

        // Prepara los headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        // Envuelve el orderRequest
        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        // Llama a la API de Kinguin para crear la orden
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                KINGUIN_ORDER_URL,
                HttpMethod.POST,
                entity,
                OrderResponse.class
        );

        OrderResponse orderResponse = response.getBody();

        if (orderResponse != null && orderResponse.getOrderId() != null) {
            System.out.println("Order placed successfully with ID: " + orderResponse.getOrderId());

            // 1. Guardamos la transacción con estado PROCESSING
            transaction.setStatus(TransactionStatus.PROCESSING);
            webSocketController.sendTransactionStatus(transaction.getStatus());

            // Opcional: guardar el orderId de Kinguin en la transacción para referencia
            transaction.setExternalOrderId(orderResponse.getOrderId());
            transactionRepository.save(transaction);

            // 2. Lanzamos el Thread de polling indefinido en el KeyPollingService
            keyPollingService.pollKeysIndefinitely(orderResponse.getOrderId(), transaction, orderRequest);

            // 3. Retornamos respuesta inmediata al cliente
            return orderResponse;
        } else {
            System.err.println("Order failed or did not return a valid Order ID.");
            throw new RuntimeException("Error placing order with Kinguin");
        }
    }

    // Resto de métodos...
    // (pollForKeys, cancelTransactionAndThrow, etc. pueden quedar obsoletos
    //  si tu nueva lógica ya no los usa)
}
