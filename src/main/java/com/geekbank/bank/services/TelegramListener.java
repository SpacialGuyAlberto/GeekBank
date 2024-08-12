package com.geekbank.bank.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final SmsService smsService;

    public TelegramListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostConstruct
    public void startListener(){
        Thread listenerThread = new Thread(this::listenForMessages);
        listenerThread.setDaemon(true);  // Daemon thread para que no bloquee la finalización de la aplicación
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

                    // Print the response to the console
                    System.out.println("Response: " + response.toString());

                    // Process the response to extract and handle messages
                    processResponse(response.toString());
                } else {
                    System.out.println("GET request failed. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Sleep to avoid hitting API rate limits
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

                    if (update.has("channel_post")) { // Check if the update contains a channel post
                        JSONObject message = update.getJSONObject("channel_post");
                        String text = message.getString("text");

                        // Cambiar de getString a getLong para los IDs
                        long chatId = message.getJSONObject("chat").getLong("id");
                        long senderChatId = message.getJSONObject("sender_chat").getLong("id");

                        System.out.println("Message from channel " + chatId + ": " + text);

                        // Actualizar lastUpdateId después de procesar todos los mensajes
                        lastUpdateId = updateId;

                        // Regex para extraer los detalles necesarios
                        Pattern pattern = Pattern.compile("/Message from 555: /Transaccion exitosa\\. /n Monto: L\\. (\\d+\\.\\d+) /n Cargos: L 0\\.00 Nombre Cliente: [A-Z ]+ /n telefono Destino: (\\+\\d+)");
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            String amount = matcher.group(1);
                            String phoneNumber = matcher.group(2);

                            System.out.println("Amount: " + amount);
                            System.out.println("MESSAGE SENT");

                            smsService.sendPaymentNotification(phoneNumber);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
