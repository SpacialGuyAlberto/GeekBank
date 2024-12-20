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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private static final String KINGUIN_ORDER_URL = "https://gateway.kinguin.net/esa/api/v1/order";
    private static final String API_KEY = "77d96c852356b1c654a80f424d67048f";
    private static final String KEYS_ENDPOINT = "https://gateway.kinguin.net/esa/api/v2/order/";

    @Autowired
    private KinguinService kinguinService;
    @Autowired
    private SendGridEmailService sendGridEmailService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private UserRepository userRepository;

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

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

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

            int maxRetries = 20; // Número máximo de intentos
            long delayMillis = 5000; // Espera de 5 segundos entre intentos
            List<Map<String, Object>> keysData = pollForKeys(orderResponse.getOrderId(), maxRetries, delayMillis, transaction);

            if (!keysData.isEmpty()) {
                List<String> keys = new ArrayList<>();
                for (Map<String, Object> keyObj : keysData) {
                    String serial = (String) keyObj.get("serial");
                    keys.add(serial);
                }

                // Asigna directamente la lista de keys
                transaction.setKeys(keys);
                transactionRepository.save(transaction);

                Optional<User> user = userRepository.findByEmail(orderRequest.getEmail());


                if (orderRequest.getEmail() != null) {
                    // Creamos el receipt
                    Receipt receipt = new Receipt(
                            transaction.getTransactionNumber(),
                            orderRequest.getEmail(),
                            "Carepija",
                            transaction.getAmountHnl(), // Puedes usar amountUsd si lo prefieres
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            orderRequest.getProducts() // Pasar la lista de productos del OrderRequest
                    );

                    // Generar el PDF en memoria
                    byte[] pdfBytes = pdfGeneratorService.generateReceiptPdfBytes(receipt);

                    // Enviar email con el PDF adjunto
                    String subject = "Recibo de tu compra - " + transaction.getTransactionNumber();
                    String body = "<p>Gracias por tu compra.</p><p>Adjunto encontrarás tu recibo en PDF.</p>";
                    String filename = "receipt_" + transaction.getTransactionNumber() + ".pdf";



                    sendGridEmailService.sendEmailWithPdfAttachment(orderRequest.getEmail(), subject, body, pdfBytes, filename);
                }

                if (orderRequest.getPhoneNumber() != null && orderRequest.getSendKeyToSMS()) {
                    smsService.sendKeysToPhoneNumber(orderRequest.getPhoneNumber(), keys);
                }

                // Opcional: enviar keys por SMS
            } else {
                System.err.println("Keys were not available after multiple attempts.");
            }

        } else {
            System.err.println("Order failed or did not return a valid Order ID.");
        }

        return orderResponse;
    }


    private List<Map<String, Object>> pollForKeys(String orderId, int maxRetries, long delayMillis, Transaction transaction) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            List<Map<String, Object>> keys = tryDownloadKeys(orderId);
            if (!keys.isEmpty()) {
                return keys;
            }
            System.out.println("No keys available yet. Attempt " + attempt + " of " + maxRetries);
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Si se interrumpe, cancelamos la transacción y lanzamos excepción
                cancelTransactionAndThrow(transaction, "Polling interrupted while waiting for keys.");
            }
        }

        // Si se agotan los intentos, cancela la transacción y lanza excepción
        cancelTransactionAndThrow(transaction, "Keys not available after maximum attempts. Transaction has been cancelled.");
        return new ArrayList<>(); // No se alcanza
    }


    private void cancelTransactionAndThrow(Transaction transaction, String message) {
        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        // Enviar al frontend un mensaje indicando el error y la necesidad de reintentar
        webSocketController.sendTransactionStatus(transaction.getStatus());

        // Lanzar la excepción para detener el proceso
        throw new RuntimeException(message);
    }


    private List<Map<String, Object>> tryDownloadKeys(String orderId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        List<Map<String, Object>> allKeys = new ArrayList<>();
        int page = 1;
        boolean morePages = true;

        while (morePages) {
            String url = KEYS_ENDPOINT + orderId + "/keys?page=" + page;
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> keyObjects = response.getBody();
            if (keyObjects == null || keyObjects.isEmpty()) {
                morePages = false;
            } else {
                allKeys.addAll(keyObjects);
                page++;
            }
        }

        return allKeys;
    }

    public String getPhoneNumberByOrderId(String orderId) {
        return "El número de teléfono asociado a la orden";
    }
}
