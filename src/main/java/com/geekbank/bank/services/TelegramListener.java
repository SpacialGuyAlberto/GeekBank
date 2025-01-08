package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.SmsMessage;
import com.geekbank.bank.models.UnmatchedPayment;
import com.geekbank.bank.repositories.*;

@Service
public class TelegramListener implements ApplicationListener<ContextClosedEvent> {

    private static final String TELEGRAM_BOT_TOKEN =
            "7022402011:AAHf6k0ZolFa9hwiZMu1srj868j5-eqUecU";
    private static final String BASE_URL =
            "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/getUpdates";

    private int lastUpdateId = 0;
    private volatile boolean running = true;

    // Inyecciones de servicios/repositories que aún quieras usar
    @Autowired
    private SmsService smsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsMessageRepository smsMessageRepository;

    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;

    public TelegramListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostConstruct
    public void startListener() {
        Thread listenerThread = new Thread(this::listenForMessages);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void listenForMessages() {
        while (running) {
            try {
                // Solicitamos updates a Telegram
                String urlWithOffset = BASE_URL + "?offset=" + (lastUpdateId + 1);
                URL url = new URL(urlWithOffset);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    processResponse(response.toString());
                } else {
                    System.out.println("GET request failed. Response Code: " + responseCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                System.err.println("El hilo de TelegramListener fue interrumpido. Saliendo del bucle.");
                break;
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.out.println("Contexto de aplicación cerrado. Deteniendo TelegramListener.");
        running = false;
    }

    void processResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray resultArray = jsonResponse.getJSONArray("result");

            if (resultArray.length() == 0) {
                return;
            }

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject update = resultArray.getJSONObject(i);
                int updateId = update.getInt("update_id");

                // Revisamos si viene en "channel_post"
                if (update.has("channel_post")) {
                    JSONObject message = update.getJSONObject("channel_post");
                    String text = message.getString("text");

                    lastUpdateId = updateId;

                    // Solo procesamos si contiene "Message from 555"
                    if (text.contains("Message from 555")) {
                        boolean matched = tryParsePayment(text);
                        if (!matched) {
                            System.out.println("Mensaje no cumple el patrón de pago.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Patrón con fecha opcional
    private static final Pattern smsPatternMegaFlexibleDateOptional = Pattern.compile(
            "(?s)"
                    + "/Message from 555:.*?"
                    + "(?:Transaccion|Transacción)(?: exitosa)?\\.?\\s*"
                    + ".*?Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                    + "Cargos:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                    + "Nombre Cliente:\\s*(.*?)\\s+"
                    + "Telefono Destino:\\s*(\\d+).*?"
                    + "Ref:\\s*(\\d+).*?"         // Captura referencia
                    + "(?:Fecha:\\s*(.*))?",     // Fecha opcional
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Intenta parsear el mensaje:
     * - Si cumple el patrón, se almacena en smsMessageRepository y UnmatchedPaymentRepository.
     * - Devuelve true si hizo match, false si no.
     */
    private boolean tryParsePayment(String text) {
        Matcher matcher = smsPatternMegaFlexibleDateOptional.matcher(text);
        if (!matcher.find()) {
            return false;
        }

        // Extraer datos
        String amountStr     = matcher.group(1);
        String cargosStr     = matcher.group(2);
        String nombreCliente = matcher.group(3);
        String telefonoDest  = matcher.group(4);
        String ref           = matcher.group(5);
        String fechaStr      = matcher.group(6); // opcional

        double amount = Double.parseDouble(amountStr);
        double cargos = Double.parseDouble(cargosStr);

        // Si no hay fecha, usar la actual
        if (fechaStr == null || fechaStr.isBlank()) {
            fechaStr = LocalDateTime.now().toLocalDate().toString();
        }

        // Crear y guardar en smsMessageRepository
        SmsMessage smsMessage = new SmsMessage(
                "555",                  // Remitente
                amount,                 // Monto
                telefonoDest,           // Tel destino
                ref,                    // Referencia
                fechaStr,               // Fecha
                "",                     // Mensaje adicional
                0.0,                    // Valor extra
                LocalDateTime.now()     // Recibido a
        );
        smsMessageRepository.save(smsMessage);

        // También guardar en UnmatchedPayment (por ahora consideramos todo como "unmatched")
        UnmatchedPayment unmatchedPayment = new UnmatchedPayment(
                smsMessage.getSenderPhoneNumber(),
                smsMessage.getAmountReceived(),
                smsMessage.getReferenceNumber(),
                smsMessage.getReceivedAt(),
                smsMessage
        );
        unmatchedPaymentRepository.save(unmatchedPayment);

        System.out.println("Se guardó el SMS y el registro de UnmatchedPayment: "
                + "Tel: " + telefonoDest
                + ", Ref: " + ref
                + ", Monto: " + amount);

        return true;
    }
}
