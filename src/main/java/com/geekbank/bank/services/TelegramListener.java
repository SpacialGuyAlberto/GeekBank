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

import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.OrderResponse;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import com.geekbank.bank.repositories.TransactionRepository;
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

    private void processResponse(String response) {
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

                        Pattern pattern = Pattern.compile("/Message from 555: /Transaccion exitosa\\. /n Monto: L\\. (\\d+\\.\\d+) /n Cargos: L 0\\.00 Nombre Cliente: [A-Z ]+ /n Ref: (\\+\\d+)");
                        //corregir a los parametros de HONDURAS
                        Pattern smsPattern = Pattern.compile(
                                "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) del (\\d{12,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) (\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
                        );


                        Matcher matcher = smsPattern.matcher(text);
                        if (matcher.find()) {
                            System.out.println("FOUND SMS MATCHING");
                            String messageFrom = matcher.group(1);
                            String amountReceived = matcher.group(2);
                            Double amountReceivedAsDouble = Double.parseDouble(amountReceived);
                            String phoneNumber = matcher.group(3);
                            String referenceNumber = matcher.group(4);
                            String date = matcher.group(5);
                            String time = matcher.group(6);
//                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
//                            LocalDateTime parsedDateTime = LocalDateTime.parse(time, formatter);
                            String newBalance = matcher.group(7);

                            //String phoneNumber = matcher.group(2);
                            System.out.println("Received Phone Number: " + phoneNumber);

                            OrderRequest orderRequest = orderRequestStorageService.getOrderRequestByPhoneNumber(phoneNumber);

                            Transaction transaction = transactionStorageService.findMatchingTransaction(phoneNumber, amountReceivedAsDouble);

//                            Transaction transaction = transactionStorageService.getTransactionByPhoneNumber(phoneNumber);
                            //verificar la suma de los productos de las transacciones
                            Transaction transactionInDB = transactionService.findByTransactionNumber(transaction.getTransactionNumber());

                            if (orderRequest != null) {
                                System.out.println("Matching Order Request found for this phone number. Processing the order...");
                                ///ACTIVAR ESTA OPCION PARA LUEGO
//                                List<String> keys = orderService.downloadKeys(orderResponse.getOrderId());
//                                smsService.sendPaymentNotification(phoneNumber);
                                try {
                                    ///ACTIVAR ESTA OPCION LUEGO
//                                    smsService.sendKeysToPhoneNumber(phoneNumber, keys);
                                    OrderResponse orderResponse = orderService.placeOrder(orderRequest);
                                    String key = "Prueba";
                                    System.out.println("Order placed with ID: " + orderResponse.getOrderId());
//                                    smsService.sendPaymentNotification(phoneNumber);
                                    transactionService.updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED);
                                    ///Hay que borrar las transacciones del ConcurrentHashmap
                                    transactionStorageService.removeTransaction(transactionInDB.getPhoneNumber());
//                                    System.out.println("Transaction status before save: " + transaction.getStatus());

                                } catch (Exception e) {
                                    System.err.println("Failed to send payment notification: " + e.getMessage());


                                    if (e.getMessage().contains("Unprocessable Entity")) {
                                        System.out.println("Insufficient balance. Cancelling transaction.");
                                        transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED);
                                    }
                                } finally {
                                    orderRequestStorageService.removeOrderRequest(phoneNumber);
                                    transactionStorageService.removeTransaction(phoneNumber);
                                    if (!orderRequestStorageService.hasOrderForPhoneNumber(phoneNumber)) {
                                        System.out.println("OrderRequest for phone number [" + phoneNumber + "] was successfully removed.");
                                    } else {
                                        System.err.println("Failed to remove OrderRequest for phone number [" + phoneNumber + "].");
                                    }
                                }
                            } else {
                                System.out.println("No matching Order Request found. Ignoring the message.");
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
