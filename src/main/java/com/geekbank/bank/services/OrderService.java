package com.geekbank.bank.services;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final String KINGUIN_ORDER_URL = "https://gateway.kinguin.net/esa/api/v1/order";
    private static final String API_KEY = "77d96c852356b1c654a80f424d67048f";

    @Autowired
    private KinguinService kinguinService;

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        // Calcular el precio total aquí o usar el valor enviado desde el frontend

        // Crear la solicitud para Kinguin API
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

        return response.getBody();
    }

    public List<String> downloadKeys(String orderId) {
        String url = "https://gateway.kinguin.net/esa/api/v2/order/" + orderId + "/keys";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map<String, String>>>() {}
        );

        List<String> keys = new ArrayList<>();
        for (Map<String, String> keyData : response.getBody()) {
            keys.add(keyData.get("serial"));
        }

        return keys;
    }

    public String getPhoneNumberByOrderId(String orderId) {
        return "El número de teléfono asociado a la orden";
    }
}
