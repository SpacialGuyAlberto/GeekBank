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
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.repositories.SmsMessageRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.UnmatchedPaymentRepository;

@Service
public class TelegramListener implements ApplicationListener<ContextClosedEvent> {

    // ----- Ajustar TOKEN y URL según tu configuración -----
    private static final String TELEGRAM_BOT_TOKEN = "7022402011:AAHf6k0ZolFa9hwiZMu1srj868j5-eqUecU";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/getUpdates";
    // ------------------------------------------------------

    private int lastUpdateId = 0;
    private volatile boolean running = true;

    @Autowired
    private SmsService smsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WebSocketController webSocketController;
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
                    String inputLine;
                    StringBuilder response = new StringBuilder();

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

                // Ajusta si usas "message" o "channel_post"
                if (update.has("channel_post")) {
                    JSONObject message = update.getJSONObject("channel_post");
                    String text = message.getString("text");
                    long chatId = message.getJSONObject("chat").getLong("id");

                    lastUpdateId = updateId;

                    // Si no contiene "Message from 555", no lo acumulamos
                    if (!text.contains("Message from 555")) {
                        // Intenta Tigo o lo que necesites...
                        // ...
                        continue;
                    }

                    // Acumular en partialMessageMap
                    StringBuilder sb = partialMessageMap
                            .computeIfAbsent(chatId, k -> new StringBuilder());

                    sb.append(" ").append(text);
                    String fullText = sb.toString().trim();

                    // Intentar parsear
                    if (checkAndProcessFrom555(fullText)) {
                        partialMessageMap.remove(chatId);
                    } else {
                        System.out.println("Partial message for chat " + chatId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkAndProcessFrom555(String fullText) {
        // Patrones en orden: Fijo, Flexible multilinea, Mega flexible
        // 1) Patrón fijo
        Pattern smsPatternFijo = Pattern.compile(
                "/Message from 555:\\s*Transaccion exitosa\\.\\s*"
                        + "Monto:\\s*L\\.\\s*(\\d+\\.\\d{2})\\s*"
                        + "Cargos:\\s*L\\.\\s*(\\d+\\.\\d{2})\\s*"
                        + "Nombre Cliente:\\s*(.*?)\\s*"
                        + "Telefono Destino:\\s*(\\d+)\\s*"
                        + "Ref:\\s*(\\d+)\\s*"
                        + "Fecha:\\s*(.*)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        // 2) Patrón flexible multilinea
        Pattern smsPatternFlexibleMultiline = Pattern.compile(
                "(?s)"
                        + "/Message from 555:.*?"
                        + "(?:Transaccion|Transaction).*?"
                        + "Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d{1,2})?).*?"
                        + "Nombre Cliente:\\s*(.*?)\\s+"
                        + "Telefono Destino:\\s*(\\d+).*?"
                        + "Ref:\\s*(\\d+).*",
                Pattern.CASE_INSENSITIVE
        );

        // 3) Patrón mega flexible
        Pattern smsPatternMegaFlexible = Pattern.compile(
                "(?s)"
                        + "/Message from 555:.*?"
                        + "(?:Transaccion|Transacción)(?: exitosa)?\\.?\\s*"
                        + ".*?Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                        + "(?:Cargos:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?)?"
                        + "Nombre Cliente:\\s*(.*?)\\s+"
                        + "Telefono Destino:\\s*(\\d+).*?"
                        + "Ref:\\s*(\\d+).*?"
                        + "(?:Fecha:\\s*(.*))?",
                Pattern.CASE_INSENSITIVE
        );

        // Intentamos cada uno
        Matcher mFijo = smsPatternFijo.matcher(fullText);
        if (mFijo.find()) {
            processPaymentFijo(mFijo);
            return true;
        }

        Matcher mFlex = smsPatternFlexibleMultiline.matcher(fullText);
        if (mFlex.find()) {
            processPaymentFlexibleMultiline(mFlex);
            return true;
        }

        Matcher mMega = smsPatternMegaFlexible.matcher(fullText);
        if (mMega.find()) {
            processPaymentMega(mMega);
            return true;
        }

        return false;
    }

    // Métodos de procesamiento

    private void processPaymentFijo(Matcher matcher) {
        String amountStr       = matcher.group(1);
        String cargosStr       = matcher.group(2);
        String nombreCliente   = matcher.group(3);
        String telefonoDestino = matcher.group(4);
        String ref             = matcher.group(5);
        String fecha           = matcher.group(6);

        double amount = Double.parseDouble(amountStr);
        double cargos = Double.parseDouble(cargosStr);

        // Construimos el objeto
        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDestino,
                ref,
                fecha,
                "",
                0.0,
                LocalDateTime.now()
        );
        smsMessageRepository.save(smsMessage);

        // Buscar transacciones pendientes
        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
        if (!transactions.isEmpty()) {
            transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
            transactionStorageService.storeAmountReceived(telefonoDestino, amount);
            webSocketController.requestRefNumberAndTempPin(telefonoDestino);
        } else {
            storeUnmatchedPayment(smsMessage);
        }
    }

    private void processPaymentFlexibleMultiline(Matcher matcher) {
        String amountStr       = matcher.group(1);
        String nombreCliente   = matcher.group(2);
        String telefonoDestino = matcher.group(3);
        String ref             = matcher.group(4);

        double amount = Double.parseDouble(amountStr);

        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDestino,
                ref,
                "",  // no capturamos fecha aquí
                "",
                0.0,
                LocalDateTime.now()
        );
        smsMessageRepository.save(smsMessage);

        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
        if (!transactions.isEmpty()) {
            transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
            transactionStorageService.storeAmountReceived(telefonoDestino, amount);
            webSocketController.requestRefNumberAndTempPin(telefonoDestino);
        } else {
            storeUnmatchedPayment(smsMessage);
        }
    }

    private void processPaymentMega(Matcher matcher) {
        String amountStr       = matcher.group(1);
        String cargosStr       = matcher.group(2); // puede ser null
        String nombreCliente   = matcher.group(3);
        String telefonoDestino = matcher.group(4);
        String ref             = matcher.group(5);
        String fechaStr        = matcher.group(6); // puede ser null

        double amount = Double.parseDouble(amountStr);
        double cargos = (cargosStr != null) ? Double.parseDouble(cargosStr) : 0.0;

        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDestino,
                ref,
                (fechaStr != null ? fechaStr : ""),
                "",
                0.0,
                LocalDateTime.now()
        );
        smsMessageRepository.save(smsMessage);

        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
        if (!transactions.isEmpty()) {
            transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
            transactionStorageService.storeAmountReceived(telefonoDestino, amount);
            webSocketController.requestRefNumberAndTempPin(telefonoDestino);
        } else {
            storeUnmatchedPayment(smsMessage);
        }
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
