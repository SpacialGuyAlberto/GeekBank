package com.geekbank.bank.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class PayPalService {

    private String clientId = "AWN0lCMoVrecKkOjVsrTCX6zG6Yjs2fE8RYupZMqND-pjJeEEbU0sNXS8l43DHSH2Q8omYqSnZ4RL9qC";
    private String clientSecret = "EKKOBZa80NAVE3IOPfEZM7AE4AEywq6RBQq8-Llbsv-VXx9bl9G6cEjcgLUPzs6sIpdjfmYvju-OHRQF";
    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Obtiene el Access Token de PayPal
     */
    public String getAccessToken() throws IOException {
        URL url = new URL(baseUrl + "/v1/oauth2/token");
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        httpConn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write("grant_type=client_credentials");
            writer.flush();
        }

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        String response = new Scanner(responseStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        String accessToken = (String) responseMap.get("access_token");

        if (accessToken == null) {
            throw new IOException("Failed to obtain access token: " + response);
        }

        return accessToken;
    }
    /**
     * Crea una Orden de Pago
     */
    public Map<String, Object> createOrder(String amount) throws IOException {
        String accessToken = getAccessToken();

        URL url = new URL(baseUrl + "/v2/checkout/orders");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        httpConn.setRequestProperty("PayPal-Request-Id", UUID.randomUUID().toString());

        // Construir el cuerpo de la solicitud JSON
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("intent", "CAPTURE");

        Map<String, Object> amountData = new HashMap<>();
        amountData.put("currency_code", "USD");
        amountData.put("value", amount);

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("reference_id", UUID.randomUUID().toString());
        purchaseUnit.put("amount", amountData);

        orderData.put("purchase_units", Collections.singletonList(purchaseUnit));

        // Añadir las URLs de retorno y cancelación
        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", "https://tusitio.com/return");
        applicationContext.put("cancel_url", "https://tusitio.com/cancel");
        orderData.put("application_context", applicationContext);

        // Convertir el mapa a JSON
        String orderJson = objectMapper.writeValueAsString(orderData);

        httpConn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream())) {
            writer.write(orderJson);
            writer.flush();
        }

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        String response = new Scanner(responseStream).useDelimiter("\\A").next();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return responseMap;
    }

    public Map<String, Object> captureOrder(String orderId) throws IOException {
        String accessToken = getAccessToken();

        URL url = new URL(baseUrl + "/v2/checkout/orders/" + orderId + "/capture");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);
        httpConn.setRequestProperty("PayPal-Request-Id", UUID.randomUUID().toString());

        // No es necesario enviar un cuerpo en esta solicitud
        httpConn.setDoOutput(true);

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        String response = new Scanner(responseStream).useDelimiter("\\A").next();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return responseMap;
    }

    public Map<String, Object> createPayout() throws IOException {
        String accessToken = getAccessToken();

        URL url = new URL(baseUrl + "/v1/payments/payouts");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);

        // Construir el cuerpo de la solicitud JSON para el Payout
        String payoutJson = "{ " +
                "\"sender_batch_header\": { " +
                "\"sender_batch_id\": \"" + UUID.randomUUID().toString() + "\", " +
                "\"email_subject\": \"Has recibido un pago\" " +
                "}, " +
                "\"items\": [ { " +
                "\"recipient_type\": \"EMAIL\", " +
                "\"amount\": { \"value\": \"10.00\", \"currency\": \"USD\" }, " +
                "\"receiver\": \"correo@destinatario.com\", " +
                "\"note\": \"Gracias por tu servicio\" " +
                "} ] " +
                "}";

        httpConn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream())) {
            writer.write(payoutJson);
            writer.flush();
        }

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        String response = new Scanner(responseStream).useDelimiter("\\A").next();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return responseMap;
    }
}
