package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
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
    private static final String TELEGRAM_BOT_TOKEN = "TU_TELEGRAM_BOT_TOKEN";
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
                // System.out.println("No new messages.");
                return;
            }

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject update = resultArray.getJSONObject(i);
                int updateId = update.getInt("update_id");

                // Ajusta según si lees "message" o "channel_post"
                if (update.has("channel_post")) {
                    JSONObject message = update.getJSONObject("channel_post");
                    String text = message.getString("text");
                    long chatId = message.getJSONObject("chat").getLong("id");

                    System.out.println("Message from channel " + chatId + ": " + text);
                    lastUpdateId = updateId;

                    // ============= PATRONES DE REGEX =============

                    // ---------------------------------------------
                    // Patrón #1 (Tigo Money) -> from \d{3}
                    // ---------------------------------------------
                    Pattern smsPatternTigo = Pattern.compile(
                            "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) "
                                    + "del (\\d{8,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) "
                                    + "(\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
                    );

                    // ---------------------------------------------
                    // Patrón #2 (Transacción exitosa “fijo” de 555)
                    // ---------------------------------------------
                    Pattern smsPatternNuevo = Pattern.compile(
                            // Ojo: forzamos "from 555"
                            "/Message from 555: Transaccion exitosa\\.\\s*"
                                    + "Monto: L\\. (\\d+\\.\\d{2})\\s*"
                                    + "Cargos: L\\. (\\d+\\.\\d{2})\\s*"
                                    + "Nombre Cliente: (.*?)\\s*"
                                    + "Telefono Destino: (\\d+)\\s*"
                                    + "Ref: (\\d+)\\s*"
                                    + "Fecha: (.*)"
                    );

                    // ---------------------------------------------
                    // Patrón #3 (Transacción exitosa *flexible*, multilinea) -> de 555
                    // ---------------------------------------------
                    Pattern smsPatternNuevoFlexible = Pattern.compile(
                            "(?s)" // DOTALL
                                    + "/Message from 555:.*?"  // EXIGE 'from 555'
                                    + "(?:Transaccion|Transaction).*?"
                                    + "Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d{1,2})?).*?"
                                    + "Nombre Cliente:\\s*(.*?)\\s+"
                                    + "Telefono Destino:\\s*(\\d+).*?"
                                    + "Ref:\\s*(\\d+).*"
                    );

                    // ---------------------------------------------
                    // Patrón #4 (Mega flexible, también de 555)
                    // ---------------------------------------------
                    Pattern smsPatternMegaFlexible = Pattern.compile(
                            "(?s)"
                                    + "/Message from 555:.*?" // Forzamos que sea from 555
                                    + "(?:Transaccion|Transacción)(?: exitosa)?\\.?\\s*"
                                    + ".*?Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?"
                                    + "(?:Cargos:\\s*L\\.\\s*(\\d+(?:\\.\\d+)?).*?)?"
                                    + "Nombre Cliente:\\s*(.*?)\\s+"
                                    + "Telefono Destino:\\s*(\\d+).*?"
                                    + "Ref:\\s*(\\d+).*?"
                                    + "(?:Fecha:\\s*(.*))?",
                            Pattern.CASE_INSENSITIVE
                    );

                    // Creamos los Matchers
                    Matcher matcherTigo          = smsPatternTigo.matcher(text);
                    Matcher matcherNuevo         = smsPatternNuevo.matcher(text);
                    Matcher matcherFlexible      = smsPatternNuevoFlexible.matcher(text);
                    Matcher matcherMegaFlexible  = smsPatternMegaFlexible.matcher(text);

                    // ---------------------------------------------
                    // #1 Tigo Money (from \d{3})
                    // ---------------------------------------------
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

                            // Guardar ref y monto
                            transactionStorageService.storeSmsReferenceNumber(senderPhoneNumber, referenceNumber);
                            transactionStorageService.storeAmountReceived(senderPhoneNumber, amountReceived);

                            webSocketController.requestRefNumberAndTempPin(senderPhoneNumber);
                        } else {
                            System.out.println("No pending transactions found for phone number: " + senderPhoneNumber + " and amount: " + amountReceived);
                            storeUnmatchedPayment(smsMessage);
                        }
                    }

                    // ---------------------------------------------
                    // #2 Transaccion exitosa “fijo” (from 555)
                    // ---------------------------------------------
                    else if (matcherNuevo.find()) {
                        System.out.println("FOUND NEW PAYMENT FORMAT MATCHING (Fijo)");
                        String amountStr       = matcherNuevo.group(1);
                        String cargosStr       = matcherNuevo.group(2);
                        String nombreCliente   = matcherNuevo.group(3);
                        String telefonoDestino = matcherNuevo.group(4);
                        String ref             = matcherNuevo.group(5);
                        String fecha           = matcherNuevo.group(6);

                        double amount = Double.parseDouble(amountStr);
                        double cargos = Double.parseDouble(cargosStr);

                        System.out.println("Nombre Cliente: " + nombreCliente);
                        System.out.println("Teléfono Destino: " + telefonoDestino);
                        System.out.println("Referencia: " + ref);
                        System.out.println("Monto: " + amount + " | Cargos: " + cargos + " | Fecha: " + fecha);

                        // Construimos el SmsMessage
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

                        // Verificar transacciones pendientes
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
                    }

                    // ---------------------------------------------
                    // #3 Transacción exitosa (flexible, multilinea) -> from 555
                    // ---------------------------------------------
                    else if (matcherFlexible.find()) {
                        System.out.println("FOUND FLEXIBLE PAYMENT FORMAT MATCHING");
                        String amountStr       = matcherFlexible.group(1);
                        String nombreCliente   = matcherFlexible.group(2);
                        String telefonoDestino = matcherFlexible.group(3);
                        String ref             = matcherFlexible.group(4);

                        double amount = Double.parseDouble(amountStr);

                        System.out.println("Nombre Cliente: " + nombreCliente);
                        System.out.println("Teléfono Destino: " + telefonoDestino);
                        System.out.println("Referencia: " + ref);
                        System.out.println("Monto: " + amount);

                        // Construimos el SmsMessage
                        SmsMessage smsMessage = new SmsMessage(
                                "555",
                                amount,
                                telefonoDestino,
                                ref,
                                "",   // date
                                "",   // time
                                0.0,
                                LocalDateTime.now()
                        );
                        smsMessageRepository.save(smsMessage);

                        // Verificar transacciones pendientes
                        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
                        if (!transactions.isEmpty()) {
                            transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
                            transactionStorageService.storeAmountReceived(telefonoDestino, amount);

                            webSocketController.requestRefNumberAndTempPin(telefonoDestino);
                        } else {
                            System.out.println("No pending transactions found for phone number: " + telefonoDestino + " and amount: " + amount);
                            storeUnmatchedPayment(smsMessage);
                        }
                    }

                    // ---------------------------------------------
                    // #4 Mega flexible (también from 555)
                    // ---------------------------------------------
                    else if (matcherMegaFlexible.find()) {
                        System.out.println("FOUND MEGA FLEXIBLE PAYMENT FORMAT MATCHING");

                        String amountStr       = matcherMegaFlexible.group(1); // Monto
                        String cargosStr       = matcherMegaFlexible.group(2); // Cargos (puede ser null)
                        String nombreCliente   = matcherMegaFlexible.group(3); // Nombre
                        String telefonoDestino = matcherMegaFlexible.group(4); // Teléfono
                        String ref             = matcherMegaFlexible.group(5); // Ref
                        String fechaStr        = matcherMegaFlexible.group(6); // Fecha (puede ser null)

                        double amount = Double.parseDouble(amountStr);
                        double cargos = (cargosStr != null) ? Double.parseDouble(cargosStr) : 0.0;

                        System.out.println("Nombre Cliente: " + nombreCliente);
                        System.out.println("Teléfono Destino: " + telefonoDestino);
                        System.out.println("Referencia: " + ref);
                        System.out.println("Monto: " + amount + " | Cargos: " + cargos + " | Fecha: " + fechaStr);

                        // Construimos el SmsMessage
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

                        // Verificar transacciones pendientes
                        List<Transaction> transactions = transactionStorageService.getPendingTransactions(telefonoDestino);
                        if (!transactions.isEmpty()) {
                            transactionStorageService.storeSmsReferenceNumber(telefonoDestino, ref);
                            transactionStorageService.storeAmountReceived(telefonoDestino, amount);

                            webSocketController.requestRefNumberAndTempPin(telefonoDestino);
                        } else {
                            System.out.println("No pending transactions found for phone number: " + telefonoDestino + " and amount: " + amount);
                            storeUnmatchedPayment(smsMessage);
                        }
                    }

                    // ---------------------------------------------
                    // Ninguno de los patrones coincidió
                    // ---------------------------------------------
                    else {
                        System.out.println("NOT FOUND SMS MATCHING");
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
        System.out.println("Stored unmatched payment for phone number: "
                + smsMessage.getSenderPhoneNumber()
                + " | Amount: " + smsMessage.getAmountReceived()
                + " | Reference: " + smsMessage.getReferenceNumber());
    }
}
