package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import com.geekbank.bank.models.Transaction;
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

    @Autowired
    private SmsService smsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketController webSocketController;

    // Map para concatenar los fragmentos de "Message from 555" por cada chatId
    private final Map<Long, StringBuilder> partialMessageMap = new ConcurrentHashMap<>();

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private AccountRepository accountRepository;

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

                // Suponiendo que lees "channel_post"
                if (update.has("channel_post")) {
                    JSONObject message = update.getJSONObject("channel_post");
                    String text = message.getString("text");
                    long chatId = message.getJSONObject("chat").getLong("id");

                    lastUpdateId = updateId;

                    // Solo acumulamos si es "Message from 555"
                    if (!text.contains("Message from 555")) {
                        continue;
                    }

                    // Acumular
                    StringBuilder sb = partialMessageMap
                            .computeIfAbsent(chatId, k -> new StringBuilder());
                    sb.append(" ").append(text);

                    String fullText = sb.toString().trim();

                    // Intentar parsear
                    if (tryParsePayment(fullText)) {
                        // Si ya parseamos y tenemos todo, limpiamos el buffer
                        partialMessageMap.remove(chatId);
                    } else {
                        System.out.println("Partial message for chat " + chatId + " => not matched yet");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Único patrón: mega flexible con fecha opcional
    private static final Pattern smsPatternMegaFlexibleDateOptional = Pattern.compile(
            "(?s)"
                    + "/Message from 555:.*?"
                    + "(?:Transaccion|Transacción)(?: exitosa)?\\.?\\s*"
                    + ".*?Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                    + "Cargos:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                    + "Nombre Cliente:\\s*(.*?)\\s+"
                    + "Telefono Destino:\\s*(\\d+).*?"
                    + "Ref:\\s*(\\d+).*?" // Captura Ref
                    + "(?:Fecha:\\s*(.*))?", // Captura fecha, si existe
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Si hace match, procesamos. Si no, devolvemos false (para seguir esperando más texto).
     */
    private boolean tryParsePayment(String text) {
        Matcher matcher = smsPatternMegaFlexibleDateOptional.matcher(text);
        if (!matcher.find()) {
            return false;
        }

        // Extraemos grupos
        String amountStr     = matcher.group(1); // Monto
        String cargosStr     = matcher.group(2); // Cargos
        String nombreCliente = matcher.group(3); // Nombre
        String telefonoDest  = matcher.group(4); // Teléfono
        String ref           = matcher.group(5); // Ref
        String fechaStr      = matcher.group(6); // Fecha (opcional)

        double amount = Double.parseDouble(amountStr);
        double cargos = Double.parseDouble(cargosStr);

        // Si no llegó fecha o está vacía, usar la de hoy:
        if (fechaStr == null || fechaStr.isBlank()) {
            fechaStr = LocalDateTime.now().toLocalDate().toString();
        }

        // Crear el SmsMessage
        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDest,
                ref,
                fechaStr,   // Aseguramos que tenga algo
                "",
                0.0,
                LocalDateTime.now()
        );
        smsMessageRepository.save(smsMessage);

        // Verificar transacciones pendientes
        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDest);
        if (!transactions.isEmpty()) {
            transactionStorageService.storeSmsReferenceNumber(telefonoDest, ref);
            transactionStorageService.storeAmountReceived(telefonoDest, amount);
            webSocketController.requestRefNumberAndTempPin(telefonoDest);
        } else {
            storeUnmatchedPayment(smsMessage);
        }

        return true;
    }

    private void storeUnmatchedPayment(SmsMessage smsMessage) {
        UnmatchedPayment unmatchedPayment = new UnmatchedPayment(
                smsMessage.getSenderPhoneNumber(),
                smsMessage.getAmountReceived(),
                smsMessage.getReferenceNumber(),
                smsMessage.getReceivedAt(),
                smsMessage
        );
        unmatchedPaymentRepository.save(unmatchedPayment);
        System.out.println("Stored unmatched payment for phone number: "
                + smsMessage.getSenderPhoneNumber()
                + " | Amount: " + smsMessage.getAmountReceived()
                + " | Reference: " + smsMessage.getReferenceNumber());
    }
}
