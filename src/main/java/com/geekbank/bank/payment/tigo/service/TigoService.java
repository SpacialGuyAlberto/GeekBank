package com.geekbank.bank.payment.tigo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.support.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class TigoService {

    private static final String apiUrl = "https://gateway.kinguin.net/esa/api/v1/order";
    private static final String apiKey = "77d96c852356b1c654a80f424d67048f";
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private SmsService smsService; // Asume que tienes un servicio para enviar SMS

//    public void placeOrderAndSendKeys(String phoneNumber, List<KinguinGiftCard> products) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("X-Api-Key", apiKey);
//        headers.set("Content-Type", "application/json");
//
//        // Construir el cuerpo de la solicitud de la orden
//        String orderRequestBody = buildOrderRequestBody(products);
//        HttpEntity<String> entity = new HttpEntity<>(orderRequestBody, headers);
//
//        // Realizar la solicitud de creación de orden
//        ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, JsonNode.class);
//        JsonNode responseBody = response.getBody();
//
//        if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
//            // Obtener el orderId de la respuesta
//            String orderId = responseBody.path("orderExternalId").asText();
//            // Descargar las claves
//            List<String> keys = downloadKeys(orderId);
//            // Enviar las claves al número de teléfono
//            smsService.sendKeysToPhoneNumber(phoneNumber, keys);
//        }
//    }

    private String buildOrderRequestBody(List<KinguinGiftCard> products) {
        StringBuilder requestBody = new StringBuilder("{ \"products\": [");
        for (KinguinGiftCard product : products) {
            requestBody.append("{")
                    .append("\"kinguinId\":").append(product.getKinguinId()).append(",")
                    .append("\"qty\":").append(product.getQty()).append(",")
                    .append("\"price\":").append(product.getPrice())
                    .append("},");
        }
        requestBody.deleteCharAt(requestBody.length() - 1); // Eliminar la última coma
        requestBody.append("]}");
        return requestBody.toString();
    }

    private List<String> downloadKeys(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String keysUrl = "https://gateway.kinguin.net/esa/api/v2/order/" + orderId + "/keys";
        ResponseEntity<JsonNode> response = restTemplate.exchange(keysUrl, HttpMethod.GET, entity, JsonNode.class);
        JsonNode responseBody = response.getBody();

        List<String> keys = new ArrayList<>();
        if (responseBody != null && responseBody.isArray()) {
            for (JsonNode keyNode : responseBody) {
                keys.add(keyNode.path("serial").asText());
            }
        }
        return keys;
    }
}
