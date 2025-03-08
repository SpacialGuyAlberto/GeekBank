package com.geekbank.bank.services;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.geekbank.bank.controllers.WebSocketController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KeyPollingService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SendGridEmailService sendGridEmailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private WebSocketController webSocketController;

    private static final String API_KEY = "77d96c852356b1c654a80f424d67048f";
    private static final String KEYS_ENDPOINT = "https://gateway.kinguin.net/esa/api/v2/order/";

    public void pollKeysIndefinitely(String orderId, Transaction transaction, OrderRequest orderRequest) {

        new Thread(() -> {

            List<Map<String, Object>> keysData = new ArrayList<>();

            while (keysData.isEmpty()) {

                keysData = tryDownloadKeys(orderId);

                if (keysData.isEmpty()) {
                    try {

                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Polling Thread Interrumpido");
                        return;
                    }
                }
            }

            List<String> keys = new ArrayList<>();
            for (Map<String, Object> keyObj : keysData) {
                String serial = (String) keyObj.get("serial");
                keys.add(serial);
            }

            transaction.setKeys(keys);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            webSocketController.sendTransactionStatus(transaction.getStatus());
            System.out.println("Keys finalmente encontradas. Se envían al usuario.");

            enviarEmailYsms(orderRequest, transaction, keys);

        }).start();
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

    private void enviarEmailYsms(OrderRequest orderRequest, Transaction transaction, List<String> keys) {
        // Envía el PDF, la lista de keys, etc., igual que ya lo haces en tu `OrderService`.
        // ...

        Optional<User> userOpt = userRepository.findByEmail(orderRequest.getEmail());
        if (orderRequest.getEmail() != null) {
            // Creamos el receipt
            Receipt receipt = new Receipt(
                    transaction.getTransactionNumber(),
                    String.valueOf(orderRequest.getUserId()),
                    orderRequest.getEmail(),
                    transaction.getAmountHnl(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    orderRequest.getProducts()
            );

            byte[] pdfBytes = pdfGeneratorService.generateReceiptPdfBytes(receipt);

            String subject = "Recibo de tu compra - " + transaction.getTransactionNumber();
            String body = "<p>Gracias por tu compra. Adjuntamos tus claves y recibo en PDF.</p>";
            String filename = "receipt_" + transaction.getTransactionNumber() + ".pdf";

            sendGridEmailService.sendEmail(orderRequest.getEmail(), subject, body, keys, transaction, pdfBytes, filename);
            sendGridEmailService.sendEmail("info@astralisbank.com", subject, body, keys, transaction, pdfBytes, filename);
        }

        if (orderRequest.getPhoneNumber() != null && orderRequest.getSendKeyToSMS()) {
            smsService.sendKeysToPhoneNumber(orderRequest.getPhoneNumber(), keys);
        }
    }
}

