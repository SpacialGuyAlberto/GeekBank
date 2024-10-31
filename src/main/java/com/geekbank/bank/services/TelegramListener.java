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
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import com.geekbank.bank.controllers.WebSocketController;

@Service
public class TelegramListener {

    private static final String TELEGRAM_BOT_TOKEN = "7022402011:AAHf6k0ZolFa9hwiZMu1srj868j5-eqUecU";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/getUpdates";
    private int lastUpdateId = 0;
    private String orderRequestPhoneNumber;


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
        while (true) {
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
            }
        }
    }

    void processResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray resultArray = jsonResponse.getJSONArray("result");

            if (resultArray.length() == 0) {
                System.out.println("No new messages.");
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

                        // Patrón para mensajes de SMS
                        Pattern smsPattern = Pattern.compile(
                                "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) del (\\d{8,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) (\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
                        );

                        Matcher matcher = smsPattern.matcher(text);
                        if (matcher.find()) {
                            System.out.println("FOUND SMS MATCHING");
                            String messageFrom = matcher.group(1);
                            String amountReceivedStr = matcher.group(2);
                            double amountReceived = Double.parseDouble(amountReceivedStr);
                            String senderPhoneNumber = matcher.group(3);
                            String referenceNumber = matcher.group(4);
                            String date = matcher.group(5);
                            String time = matcher.group(6);
                            String newBalanceStr = matcher.group(7);
                            double newBalance = Double.parseDouble(newBalanceStr);

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

                            // Buscar transacciones pendientes que coincidan
                            List<Transaction> transactions = transactionStorageService.getPendingTransactions(senderPhoneNumber);

                            if (!transactions.isEmpty()) {
                                System.out.println("Found " + transactions.size() + " pending transactions for phone number: " + senderPhoneNumber);

                                // Almacenar el número de referencia y monto recibido
                                transactionStorageService.storeSmsReferenceNumber(senderPhoneNumber, referenceNumber);
                                transactionStorageService.storeAmountReceived(senderPhoneNumber, amountReceived);

                                // Enviar solicitud al frontend para que el usuario ingrese el PIN y el número de referencia
                                webSocketController.requestRefNumberAndTempPin(senderPhoneNumber);

                                // Asociar el SmsMessage con la transacción
                                Transaction matchingTransaction = transactions.get(0); // Puedes mejorar la lógica de selección

                                // Verificar si la transacción ya tiene un SmsMessage asociado
                                if (matchingTransaction.getSmsMessage() == null) {
                                    smsMessage.setTransaction(matchingTransaction);
                                    smsMessageRepository.save(smsMessage);
                                    System.out.println("Associated SmsMessage with Transaction ID: " + matchingTransaction.getId());
                                } else {
                                    System.out.println("Transaction ID: " + matchingTransaction.getId() + " already has an associated SmsMessage.");
                                    // Opcional: Manejar el caso donde ya existe una asociación
                                }
                            } else {
                                System.out.println("No pending transactions found for phone number: " + senderPhoneNumber + " and amount: " + amountReceived);

                                // Almacenar el pago no coincidente
                                storeUnmatchedPayment(smsMessage);
                            }
                        } else {
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
