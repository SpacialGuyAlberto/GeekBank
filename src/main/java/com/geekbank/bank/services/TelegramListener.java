package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.AccountRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

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
                Thread.sleep(1000);
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
                                "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) del (\\d{12,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) (\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
                        );

                        Matcher matcher = smsPattern.matcher(text);
                        if (matcher.find()) {
                            System.out.println("FOUND SMS MATCHING");
                            String messageFrom = matcher.group(1);
                            String amountReceived = matcher.group(2);
                            double amountReceivedAsDouble = Double.parseDouble(amountReceived);
                            String phoneNumber = matcher.group(3);
                            String referenceNumber = matcher.group(4);
                            String date = matcher.group(5);
                            String time = matcher.group(6);
                            String newBalance = matcher.group(7);

                            System.out.println("Received Phone Number: " + phoneNumber);

                            // Obtener la OrderRequest y la Transaction asociadas
                            OrderRequest orderRequest = orderRequestStorageService.getOrderRequestByPhoneNumber(phoneNumber);
                            Transaction transaction = transactionStorageService.findMatchingTransaction(phoneNumber);

                            if (transaction != null) {
                                Transaction transactionInDB = transactionService.findByTransactionNumber(transaction.getTransactionNumber());

                                if (transactionInDB != null && orderRequest != null) {
                                    System.out.println("Matching Order Request found for this phone number. Processing the order...");

                                    try {
                                        if (amountReceivedAsDouble < transactionInDB.getAmount()) {
                                            String failureReason = "Monto recibido insuficiente.";
                                            transactionService.updateTransactionStatus(transactionInDB.getId(), TransactionStatus.FAILED, failureReason);
                                            System.err.println("Failed to process transaction: " + failureReason);
                                            // Remover la OrderRequest del almacenamiento temporal
                                            orderRequestStorageService.removeOrderRequest(phoneNumber);
                                            continue;
                                        }

                                        if (transactionInDB.getType() == TransactionType.BALANCE_PURCHASE) {
                                            // **Caso de compra de balance**
                                            User user = transactionInDB.getUser();
                                            if (user == null) {
                                                throw new RuntimeException("Usuario no encontrado para la transacción.");
                                            }
                                            Account account = user.getAccount();
                                            if (account == null) {
                                                throw new RuntimeException("Cuenta no encontrada para el usuario.");
                                            }

                                            account.setBalance(account.getBalance() + transactionInDB.getAmount());
                                            accountRepository.save(account);

                                            transactionService.updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED, null);
                                            System.out.println("Balance updated for user: " + user.getEmail());

                                            // Opcional: Enviar notificación al usuario
                                            // smsService.sendBalanceUpdatedNotification(phoneNumber);

                                        } else if (transactionInDB.getType() == TransactionType.PURCHASE) {
                                            // **Caso de compra de productos**
                                            OrderResponse orderResponse = orderService.placeOrder(orderRequest);
                                            System.out.println("Order placed with ID: " + orderResponse.getOrderId());

                                            transactionService.updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED, null);

                                            // Opcional: Enviar notificación al usuario
                                            // smsService.sendPaymentNotification(phoneNumber);
                                        } else {
                                            // Otros tipos de transacciones si aplica
                                            System.out.println("Unknown transaction type. Skipping.");
                                        }

                                        // Remover transacción del almacenamiento temporal
                                        transactionStorageService.removeTransaction(transactionInDB.getPhoneNumber());

                                    } catch (Exception e) {
                                        String failureReason = "Error al procesar la transacción: " + e.getMessage();
                                        System.err.println("Failed to process transaction: " + failureReason);
                                        transactionService.updateTransactionStatus(transactionInDB.getId(), TransactionStatus.FAILED, failureReason);
                                    } finally {
                                        // Remover la OrderRequest del almacenamiento temporal
                                        orderRequestStorageService.removeOrderRequest(phoneNumber);

                                        if (!orderRequestStorageService.hasOrderForPhoneNumber(phoneNumber)) {
                                            System.out.println("OrderRequest for phone number [" + phoneNumber + "] was successfully removed.");
                                        } else {
                                            System.err.println("Failed to remove OrderRequest for phone number [" + phoneNumber + "].");
                                        }
                                    }
                                } else {
                                    System.out.println("No matching Order Request or Transaction found. Ignoring the message.");
                                }
                            } else {
                                System.out.println("No matching Transaction found. Ignoring the message.");
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
}
