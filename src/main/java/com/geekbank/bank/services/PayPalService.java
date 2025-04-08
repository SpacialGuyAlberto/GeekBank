package com.geekbank.bank.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import com.paypal.sdk.models.PayeePaymentMethodPreference;
import com.paypal.sdk.models.PaymentMethodPreference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PayPalService {

    private final PaypalServerSdkClient client;
    private final ObjectMapper objectMapper;

    @Value("${PAYPAL_BASE_URL}")
    private String baseUrl;

    @Value("${PAYPAL_CLIENT_ID}")
    private String clientId;

    @Value("${PAYPAL_CLIENT_SECRET}")
    private String clientSecret;

    public PayPalService(PaypalServerSdkClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Crea una orden de PayPal usando el SDK oficial.
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

//        public Map<String, Object> createOrder(String amount) throws IOException {
//        String accessToken = getAccessToken();
//
//        URL url = new URL(baseUrl + "/v2/checkout/orders");
//        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
//        httpConn.setRequestMethod("POST");
//
//        httpConn.setRequestProperty("Content-Type", "application/json");
//        httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);
//        httpConn.setRequestProperty("PayPal-Request-Id", UUID.randomUUID().toString());
//
//        // Construir el cuerpo de la solicitud JSON
//        Map<String, Object> orderData = new HashMap<>();
//        orderData.put("intent", "CAPTURE");
//
//        Map<String, Object> amountData = new HashMap<>();
//        amountData.put("currency_code", "USD");
//        amountData.put("value", amount);
//
//        Map<String, Object> purchaseUnit = new HashMap<>();
//        purchaseUnit.put("reference_id", UUID.randomUUID().toString());
//        purchaseUnit.put("amount", amountData);
//
//        orderData.put("purchase_units", Collections.singletonList(purchaseUnit));
//
//        // Añadir las URLs de retorno y cancelación
//        Map<String, Object> applicationContext = new HashMap<>();
//        applicationContext.put("return_url", "https://astralisbank.com/home");
//        applicationContext.put("cancel_url", "https://tusitio.com/cancel");
//        orderData.put("application_context", applicationContext);
//
//        // Convertir el mapa a JSON
//        String orderJson = objectMapper.writeValueAsString(orderData);
//
//        httpConn.setDoOutput(true);
//        try (OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream())) {
//            writer.write(orderJson);
//            writer.flush();
//        }
//
//        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
//                ? httpConn.getInputStream()
//                : httpConn.getErrorStream();
//        String response = new Scanner(responseStream).useDelimiter("\\A").next();
//
//        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
//        return responseMap;
//    }
    
    public Map<String, Object> createOrder(String amount) throws IOException, ApiException {
        // 1. Se construye el objeto de la orden (OrderRequest).
        //    En este ejemplo solo pasamos un PurchaseUnitRequest con un AmountWithBreakdown.
        System.out.println( "CLIENT ID ////////////////" + clientId);

//        OrderRequest orderRequest = new OrderRequest.Builder(
//                CheckoutPaymentIntent.fromString("CAPTURE"),
//                Arrays.asList(
//                        new PurchaseUnitRequest.Builder(
//                                new AmountWithBreakdown.Builder("USD", amount)
//                                        .build()
//                        )
//                                .referenceId(UUID.randomUUID().toString())
//                                .build()
//                )
//        ).build();
        PaymentMethodPreference paymentMethodPref = new PaymentMethodPreference.Builder()
                .payeePreferred(PayeePaymentMethodPreference.UNRESTRICTED)
                .build();

        OrderRequest orderRequest = new OrderRequest.Builder(
                CheckoutPaymentIntent.CAPTURE,
                Collections.singletonList(
                        new PurchaseUnitRequest.Builder(
                                new AmountWithBreakdown.Builder("USD", amount).build()
                        ).referenceId(UUID.randomUUID().toString()).build()
                )
                )
                .applicationContext(
                        new OrderApplicationContext.Builder()
                                .returnUrl("https://astralisbank.com/home")
                                .cancelUrl("https://astralisbank.com/home")
                                .paymentMethod(paymentMethodPref)
                                .build()
                )
                .build();


        // 2. Crear el input para la orden.
        OrdersCreateInput createOrderInput = new OrdersCreateInput.Builder(null, orderRequest).build();

        // 3. Obtener el OrdersController para llamar al método createOrder del SDK.
        OrdersController ordersController = client.getOrdersController();

        // 4. Llamar al método createOrder.
        ApiResponse<Order> apiResponse = ordersController.ordersCreate(createOrderInput);
        Order order = apiResponse.getResult();

        // 5. Convertimos la respuesta Order en un Map para retornarlo como JSON.
        return objectMapper.convertValue(order, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Obtiene la información completa de la orden (incluyendo status) desde PayPal.
     */
    public Order getOrderDetails(String orderId) throws ApiException, IOException {
        OrdersController ordersController = client.getOrdersController();

        // Construimos la request para obtener la orden:
        OrdersGetInput getInput = new OrdersGetInput.Builder(orderId).build();

        // Llamamos al endpoint GET /v2/checkout/orders/{orderId}
        ApiResponse<Order> response = ordersController.ordersGet(getInput);
        return response.getResult();
    }

    /**
     * Captura una orden existente de PayPal usando el SDK oficial,
     * verificando antes que el estado de la orden sea APPROVED.
     */
//    public Map<String, Object> captureOrder(String orderId) throws IOException, ApiException {
//        // 1. Verificar estado de la orden antes de capturar
//        Order existingOrder = getOrderDetails(orderId);
//        OrderStatus status = existingOrder.getStatus();
//        System.out.println("Current order status: " + status);
//
//        if (!"APPROVED".equalsIgnoreCase(status.toString())) {
//            throw new IllegalStateException(
//                    "No se puede capturar la orden. " +
//                            "Se requiere que esté en estado APPROVED, y está en: " + status
//            );
//        }
//
//        // 2. Construir el input para captura de la orden
//        OrdersCaptureInput captureOrderInput = new OrdersCaptureInput.Builder(orderId, null).build();
//
//        // 3. Capturar la orden con el SDK
//        OrdersController ordersController = client.getOrdersController();
//        ApiResponse<Order> apiResponse = ordersController.ordersCapture(captureOrderInput);
//        Order capturedOrder = apiResponse.getResult();
//
//        // 4. Convertir la respuesta a Map y retornarla
//        return objectMapper.convertValue(capturedOrder, new TypeReference<Map<String, Object>>() {});
//    }
//
//    /**
//     * Ejemplo de payout (opcional, si lo necesitas).
//     * Con el nuevo SDK se manejaría diferente;
//     * si todavía no hay un controlador de payouts en el SDK,
//     * podrías seguir usando la llamada manual aquí.
//     */
//    public Map<String, Object> createPayout() {
//        // Por ahora, si tu SDK no tiene método para payouts,
//        // se haría como antes o con un endpoint si PayPal provee uno.
//        return Collections.emptyMap();
//    }
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
