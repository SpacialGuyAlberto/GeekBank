package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.SmsMessageRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.repositories.UnmatchedPaymentRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import com.geekbank.bank.controllers.WebSocketController;

@Service
public class TelegramListener implements ApplicationListener<ContextClosedEvent> {

    private static final String TELEGRAM_BOT_TOKEN = "7022402011:AAHf6k0ZolFa9hwiZMu1srj868j5-eqUecU";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/getUpdates";
    private int lastUpdateId = 0;
    private String orderRequestPhoneNumber;
    private volatile boolean running = true;

    @Autowired
    private SmsService smsService;

    @Autowired
    private OrderService orderService;

    @Autowired WebSocketController webSocketController;

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
    public void startListener(){
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

//                    System.out.println("Response: " + response.toString());

                    processResponse(response.toString());
                } else {
                    System.out.println("GET request failed  . Response Code: " + responseCode);
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
                // System.out.println("No new messages.");
            } else {
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject update = resultArray.getJSONObject(i);
                    int updateId = update.getInt("update_id");

                    if (update.has("channel_post")) {
                        JSONObject message = update.getJSONObject("channel_post");
                        String text = message.getString("text");

                        long chatId = message.getJSONObject("chat").getLong("id");

                        System.out.println("Message from channel " + chatId + ": " + text);

                        lastUpdateId = updateId;

                        // ============= PATRONES DE REGEX =============

                        // Patrón #1 (Tigo Money)
                        Pattern smsPatternTigo = Pattern.compile(
                                "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) " +
                                        "del (\\d{8,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) " +
                                        "(\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
                        );

                        // Patrón #2 (Nuevo formato “Transacción exitosa”)
                        Pattern smsPatternNuevo = Pattern.compile(
                                "/Message from 555: Transaccion exitosa\\.\\s*" +         // /Message from 555: Transaccion exitosa.
                                        "Monto: L\\. (\\d+\\.\\d{2})\\s*" +                       // Monto: L. 75.00
                                        "Cargos: L\\. (\\d+\\.\\d{2})\\s*" +                      // Cargos: L. 0.00
                                        "Nombre Cliente: (.*?)\\s*" +                             // Nombre Cliente: (captura texto con espacios)
                                        "Telefono Destino: (\\d+)\\s*" +                          // Telefono Destino: 96631493
                                        "Ref: (\\d+)\\s*" +                                       // Ref: 104162899
                                        "Fecha: (.*)"                                             // Fecha: 01/0 (usamos .* para que no truene si viene truncada)
                        );

                        Matcher matcherTigo = smsPatternTigo.matcher(text);
                        Matcher matcherNuevo = smsPatternNuevo.matcher(text);

                        // Primero intentamos con el patrón de Tigo Money
                        if (matcherTigo.find()) {
                            System.out.println("FOUND TIGO MONEY MATCHING");
                            String messageFrom       = matcherTigo.group(1);
                            String amountReceivedStr = matcherTigo.group(2);
                            double amountReceived    = Double.parseDouble(amountReceivedStr);
                            String senderPhoneNumber = matcherTigo.group(3);
                            String referenceNumber   = matcherTigo.group(4);
                            String date              = matcherTigo.group(5);
                            String time              = matcherTigo.group(6);
                            String newBalanceStr     = matcherTigo.group(7);
                            double newBalance        = Double.parseDouble(newBalanceStr);

                            System.out.println("Received Phone Number: " + senderPhoneNumber);
                            System.out.println("Amount Received: " + amountReceived);
                            System.out.println("Reference Number: " + referenceNumber);

                            // Crear y guardar el SmsMessage
                            SmsMessage smsMessage = new SmsMessage(
                                    messageFrom,
                                    amountReceived,
                                    senderPhoneNumber,
                                    referenceNumber,
                                    date,
                                    time,
                                    newBalance,
                                    LocalDateTime.now()
                            );
                            smsMessageRepository.save(smsMessage);

                            // Verificar transacciones pendientes
                            List<Transaction> transactions = transactionStorageService.getPendingTransactions(senderPhoneNumber);
                            if (!transactions.isEmpty()) {
                                System.out.println("Found " + transactions.size() + " pending transactions for phone number: " + senderPhoneNumber);

                                // Guardar ref y monto para luego pedir PIN temporal
                                transactionStorageService.storeSmsReferenceNumber(senderPhoneNumber, referenceNumber);
                                transactionStorageService.storeAmountReceived(senderPhoneNumber, amountReceived);

                                webSocketController.requestRefNumberAndTempPin(senderPhoneNumber);

                            } else {
                                System.out.println("No pending transactions found for phone number: " + senderPhoneNumber + " and amount: " + amountReceived);
                                // Almacena el pago no coincidente
                                storeUnmatchedPayment(smsMessage);
                            }

                        }
                        // Si no coincide con Tigo Money, probamos el patrón nuevo.
                        else if (matcherNuevo.find()) {
                            System.out.println("FOUND NEW PAYMENT FORMAT MATCHING");
                            // Grupos capturados según el patrón
                            String amountStr      = matcherNuevo.group(1);
                            String cargosStr      = matcherNuevo.group(2);
                            String nombreCliente  = matcherNuevo.group(3);
                            String telefonoDestino= matcherNuevo.group(4);
                            String ref            = matcherNuevo.group(5);
                            String fecha          = matcherNuevo.group(6);

                            double amount = Double.parseDouble(amountStr);
                            double cargos = Double.parseDouble(cargosStr);

                            // Aquí podrías crear un objeto similar (SmsMessage o el que tú uses)
                            // o guardar estos datos directamente:
                            System.out.println("Nombre Cliente: " + nombreCliente);
                            System.out.println("Teléfono Destino: " + telefonoDestino);
                            System.out.println("Referencia: " + ref);
                            System.out.println("Monto: " + amount + " | Cargos: " + cargos + " | Fecha: " + fecha);

                            // Supongamos que, en tu lógica, deseas tratarlo igual que un SMS de pago.
                            // Creas un SmsMessage, aunque el "senderPhoneNumber" sea "555" por convención.
                            SmsMessage smsMessage = new SmsMessage(
                                    "555",                // messageFrom (fijo, en este caso)
                                    amount,              // amountReceived
                                    telefonoDestino,     // phoneNumber interpretado como "origen/destino"
                                    ref,                 // referenceNumber
                                    fecha,               // date
                                    "",                  // time (no lo tenemos, puedes dejarlo vacío o ajustar)
                                    0.0,                 // newBalance (no se provee)
                                    LocalDateTime.now()  // Fecha/hora actual de recepción
                            );
                            smsMessageRepository.save(smsMessage);

                            // Verificas si hay transacciones pendientes con ese teléfono (o la lógica que uses).
                            List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
                            if (!transactions.isEmpty()) {
                                System.out.println("Found " + transactions.size() + " pending transactions for phone number: " + telefonoDestino);

                                transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
                                transactionStorageService.storeAmountReceived(telefonoDestino, amount);

                                webSocketController.requestRefNumberAndTempPin(telefonoDestino);

                            } else {
                                System.out.println("No pending transactions found for phone number: " + telefonoDestino + " and amount: " + amount);
                                storeUnmatchedPayment(smsMessage);
                            }

                        } else {
                            // No coincide con ninguno de los dos patrones
                            System.out.println("NOT FOUND SMS MATCHING");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("Stored unmatched payment for phone number: " + smsMessage.getSenderPhoneNumber() + " | Amount: " + smsMessage.getAmountReceived() + " | Reference: " + smsMessage.getReferenceNumber());
    }
}
