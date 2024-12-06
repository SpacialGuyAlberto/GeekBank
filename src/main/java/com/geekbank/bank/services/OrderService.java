package com.geekbank.bank.services;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.OrdersRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    private static final String KINGUIN_ORDER_URL = "https://gateway.kinguin.net/esa/api/v1/order";
    private static final String API_KEY = "77d96c852356b1c654a80f424d67048f";
    private static final String KEYS_ENDPOINT = "https://gateway.kinguin.net/esa/api/v2/order/";

    @Autowired
    private KinguinService kinguinService;
    @Autowired
    private SendGridEmailService sendGridEmailService;
    @Autowired
    private SmsService smsService;


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

            // Aquí implementamos el mecanismo de polling:
            // Intentamos varias veces descargar las keys hasta que estén disponibles.
            int maxRetries = 10; // Número máximo de intentos
            long delayMillis = 5000; // Espera de 5 segundos entre intentos
            List<Map<String, Object>> keysData = pollForKeys(orderResponse.getOrderId(), maxRetries, delayMillis);

            if (!keysData.isEmpty()) {
                // Extrae sólo el "serial" si es que quieres enviarlo como texto plano
                // o envía toda la data JSON.
                List<String> keys = new ArrayList<>();
                for (Map<String, Object> keyObj : keysData) {
                    String serial = (String) keyObj.get("serial");
                    keys.add(serial);
                }

                transaction.setKeys(keys);
                transactionRepository.save(transaction);
                // Enviar correo con las keys
                sendGridEmailService.sendPurchaseConfirmationEmail("enkiluzlbel@gmail.com", keys, transaction);
                // Opcional: enviar keys por SMS
                //String phoneNumber = orderRequest.getPhoneNumber();
                //smsService.sendKeysToPhoneNumber(phoneNumber, keys);
            } else {
                System.err.println("Keys were not available after multiple attempts.");
            }

        } else {
            System.err.println("Order failed or did not return a valid Order ID.");
        }

        return orderResponse;
    }

    /**
     * Intenta descargar las keys varias veces, esperando cierto tiempo entre intentos.
     * @param orderId
     * @param maxRetries número máximo de intentos
     * @param delayMillis tiempo de espera entre intentos en milisegundos
     * @return Lista de keys si disponibles, o lista vacía si no se obtuvieron
     */
    private List<Map<String, Object>> pollForKeys(String orderId, int maxRetries, long delayMillis) {
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
                break;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Intento de descarga de keys (simplemente llama al endpoint, si no hay keys, retorna lista vacía)
     */
    private List<Map<String, Object>> tryDownloadKeys(String orderId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        // Comenzamos desde la página 1
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
