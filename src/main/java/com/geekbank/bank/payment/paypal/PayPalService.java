package com.geekbank.bank.payment.paypal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Servicio PayPal para **Expanded Checkout**.
 * Siempre trabajamos con **intent = CAPTURE** y contra el entorno LIVE.
 */
@Service
public class PayPalService {

    /* ---------- Credenciales (inyectadas desde application‑*.yml) ---------- */
    private final String clientId;
    private final String clientSecret;

    /* ---------- SDK Client (una sola instancia) ---------- */
    private final PaypalServerSdkClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    public PayPalService(@Value("${PAYPAL_CLIENT_ID}")     String clientId,
                         @Value("${PAYPAL_CLIENT_SECRET}") String clientSecret) {
        this.clientId     = clientId;
        this.clientSecret = clientSecret;

        /* Forzamos el entorno LIVE (producción) */
        this.client = new PaypalServerSdkClient.Builder()
                .environment(Environment.PRODUCTION)   //  ← ← LIVE
                .clientCredentialsAuth(new ClientCredentialsAuthModel.Builder(clientId, clientSecret).build())
                .loggingConfig(b -> b.level(Level.INFO))
                .build();
    }

    /* ====================== 1 · CREATE ORDER ====================== */
    public Map<String, Object> createOrder(String amountStr) throws IOException, ApiException {
        BigDecimal amount = new BigDecimal(amountStr);

        PurchaseUnitRequest pu = new PurchaseUnitRequest.Builder(
                new AmountWithBreakdown.Builder("EUR", amount.toPlainString()).build()
        ).build();

        OrderRequest orderReq = new OrderRequest.Builder(
                CheckoutPaymentIntent.CAPTURE,
                List.of(pu)
        ).build();

        OrdersCreateInput input = new OrdersCreateInput.Builder(null, orderReq).build();
        OrdersController orders = client.getOrdersController();
        ApiResponse<Order> resp  = orders.ordersCreate(input);

        return mapper.convertValue(resp.getResult(), new TypeReference<>() {});
    }

    /* ====================== 2 · CAPTURE ORDER ===================== */
    public Map<String, Object> captureOrder(String orderId) throws IOException, ApiException {
        OrdersController oc = client.getOrdersController();
        OrdersCaptureInput in = new OrdersCaptureInput.Builder(orderId, null).build();

        ApiResponse<Order> resp = oc.ordersCapture(in);   // ⇠ NO getResult()
        return mapper.convertValue(resp.getResult(), new TypeReference<>() {});
    }


    /* ------------------------------------------------------------------ */
    /* Métodos de Hosted‑Fields conservados en blanco para compatibilidad */
    /* ------------------------------------------------------------------ */
    public Map<String, Object> confirmPaymentSource(String id, Map<String, Object> b) { return Map.of(); }
    public Map<String, Object> authorizeOrder(String id)                    { return Map.of(); }
    public Map<String, Object> captureAuthorization(String id)              { return Map.of(); }
    public Map<String, Object> confirmOrder(String id)                      { return Map.of(); }
}
