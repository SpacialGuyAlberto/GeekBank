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
    private static final String TELEGRAM_BOT_TOKEN = "TU_TELEGRAM_BOT_TOKEN";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/getUpdates";
    // ------------------------------------------------------

    private int lastUpdateId = 0;
    private volatile boolean running = true;

    // Buffer para mensajes parciales, indexado por chatId
    // Cuando lleguen fragmentos de "from 555", se irán concatenando
    private final Map<Long, StringBuilder> partialMessageMap = new ConcurrentHashMap<>();

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
                // No new messages
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

                    lastUpdateId = updateId;

                    // Si no es un mensaje "from 555", intentamos reconocer Tigo u otro
                    if (!text.contains("Message from 555")) {
                        boolean recognized = checkTigoPattern(text);
                        if (!recognized) {
                            // No es Tigo ni from 555, puedes decidir guardarlo como unmatched o ignorarlo
                            System.out.println("Message is neither from 555 nor Tigo. Ignoring or storing as unmatched.");
                        }
                        continue;
                    }

                    // Si es "from 555", concatenamos en el buffer de ese chat
                    StringBuilder partialBuilder = partialMessageMap.computeIfAbsent(chatId, k -> new StringBuilder());
                    partialBuilder.append(" ").append(text); // agregamos con un espacio en medio

                    // Intentamos parsear con todo lo que tenemos acumulado
                    String fullText = partialBuilder.toString().trim();

                    // Si se logró parsear, limpiamos el buffer
                    if (checkAndProcessFrom555(fullText, chatId)) {
                        partialMessageMap.remove(chatId);
                    } else {
                        // Aún no matchea, seguimos esperando más fragmentos
                        System.out.println("Still partial message for chat " + chatId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si el texto corresponde a TigoMoney (from \d{3}) en un solo mensaje
     */
    private boolean checkTigoPattern(String text) {
        // Patrón Tigo
        Pattern smsPatternTigo = Pattern.compile(
                "/Message from (\\d{3}): Has recibido L (\\d{2,}\\.\\d{2}) "
                        + "del (\\d{8,13})\\. Ref\\. (\\d{9,10}), Fecha: (\\d{2}/\\d{2}/\\d{2}) "
                        + "(\\d{2}:\\d{2}) Nuevo balance Tigo Money: L (\\d{2,}\\.\\d{2})"
        );
        Matcher matcher = smsPatternTigo.matcher(text);
        if (matcher.find()) {
            System.out.println("FOUND TIGO MONEY MATCHING (single message)");
            // Procesar Tigo
            String messageFrom       = matcher.group(1);
            String amountReceivedStr = matcher.group(2);
            double amountReceived    = Double.parseDouble(amountReceivedStr);
            String senderPhoneNumber = matcher.group(3);
            String referenceNumber   = matcher.group(4);
            String date              = matcher.group(5);
            String time              = matcher.group(6);
            String newBalanceStr     = matcher.group(7);
            double newBalance        = Double.parseDouble(newBalanceStr);

            // Guarda en BD
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

            // Checa transacciones pendientes
            List<Transaction> transactions = transactionStorageService.getPendingTransactions(senderPhoneNumber);
            if (!transactions.isEmpty()) {
                transactionStorageService.storeSmsReferenceNumber(senderPhoneNumber, referenceNumber);
                transactionStorageService.storeAmountReceived(senderPhoneNumber, amountReceived);
                webSocketController.requestRefNumberAndTempPin(senderPhoneNumber);
            } else {
                storeUnmatchedPayment(smsMessage);
            }

            return true;
        }
        return false;
    }

    /**
     * Trata de procesar (acumulado) "from 555" con alguno de los patrones.
     * Retorna true si alguno matcheó y fue procesado, false si no.
     */
    private boolean checkAndProcessFrom555(String fullText, long chatId) {
        // Patrón fijo (Transaccion exitosa)
        Pattern smsPatternFijo = Pattern.compile(
                "/Message from 555: Transaccion exitosa\\.\\s*"
                        + "Monto: L\\. (\\d+\\.\\d{2})\\s*"
                        + "Cargos: L\\. (\\d+\\.\\d{2})\\s*"
                        + "Nombre Cliente: (.*?)\\s*"
                        + "Telefono Destino: (\\d+)\\s*"
                        + "Ref: (\\d+)\\s*"
                        + "Fecha: (.*)"
        );

        // Patrón flexible multilinea
        Pattern smsPatternFlexibleMultiline = Pattern.compile(
                "(?s)" // DOTALL
                        + "/Message from 555:.*?"
                        + "(?:Transaccion|Transaction).*?"
                        + "Monto:\\s*L\\.\\s*(\\d+(?:\\.\\d{1,2})?).*?"
                        + "Nombre Cliente:\\s*(.*?)\\s+"
                        + "Telefono Destino:\\s*(\\d+).*?"
                        + "Ref:\\s*(\\d+).*"
        );

        // Patrón mega flexible
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

        // Patrón #1: Fijo
        Matcher matcherFijo = smsPatternFijo.matcher(fullText);
        if (matcherFijo.find()) {
            System.out.println("FOUND from 555 (fijo) in ACCUMULATED text");
            processPaymentFijo(matcherFijo);
            return true;
        }

        // Patrón #2: Flexible multilinea
        Matcher matcherFlex = smsPatternFlexibleMultiline.matcher(fullText);
        if (matcherFlex.find()) {
            System.out.println("FOUND from 555 (flexible multiline) in ACCUMULATED text");
            processPaymentFlexibleMultiline(matcherFlex);
            return true;
        }

        // Patrón #3: Mega flexible
        Matcher matcherMega = smsPatternMegaFlexible.matcher(fullText);
        if (matcherMega.find()) {
            System.out.println("FOUND from 555 (mega flexible) in ACCUMULATED text");
            processPaymentMega(matcherMega);
            return true;
        }

        // No coincidió con ninguno
        return false;
    }

    // --------------------------
    //   Métodos para procesar
    // --------------------------

    private void processPaymentFijo(Matcher matcher) {
        String amountStr       = matcher.group(1);
        String cargosStr       = matcher.group(2);
        String nombreCliente   = matcher.group(3);
        String telefonoDestino = matcher.group(4);
        String ref             = matcher.group(5);
        String fecha           = matcher.group(6);

        double amount = Double.parseDouble(amountStr);
        double cargos = Double.parseDouble(cargosStr);

        System.out.println("Procesando Transaccion exitosa (fijo) con: "
                + amount + ", " + nombreCliente);

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

        System.out.println("Procesando Transaccion exitosa (flexible multiline) con: "
                + amount + ", " + nombreCliente);

        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDestino,
                ref,
                "",  // no captura fecha ni time
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
            storeUnmatchedPayment(smsMessage);
        }
    }

    private void processPaymentMega(Matcher matcher) {
        String amountStr       = matcher.group(1);
        String cargosStr       = matcher.group(2); // podría ser null
        String nombreCliente   = matcher.group(3);
        String telefonoDestino = matcher.group(4);
        String ref             = matcher.group(5);
        String fechaStr        = matcher.group(6); // podría ser null

        double amount = Double.parseDouble(amountStr);
        double cargos = (cargosStr != null) ? Double.parseDouble(cargosStr) : 0.0;

        System.out.println("Procesando Transaccion exitosa (mega flexible) con: "
                + amount + ", " + nombreCliente);

        SmsMessage smsMessage = new SmsMessage(
                "555",
                amount,
                telefonoDestino,
                ref,
                (fechaStr != null ? fechaStr : ""), // si no viene la fecha, ""
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
