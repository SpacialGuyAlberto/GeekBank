package com.geekbank.bank.user.admin.controller;

import com.geekbank.bank.transaction.constants.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private TransactionStatus transactionStatus;
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTransactionStatus(String phoneNumber, String status, String message, String transactionId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("message", message);
        payload.put("transactionId", transactionId);
        messagingTemplate.convertAndSend("/topic/transaction-status2/" + phoneNumber, payload);
        System.out.println("Transaction update sent to WebSocket for phone number: " + phoneNumber + " | Status: " + status);
    }

    public void sendTransactionStatus(TransactionStatus status){
        Map<String, String> payload = new HashMap<>();
        payload.put("status", String.valueOf(status));

        messagingTemplate.convertAndSend("/topic/transaction-status", payload);
    }

    public void requestRefNumberAndTempPin(String phoneNumber) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Por favor, ingrese su PIN y el número de referencia de su pago para verificar su transacción.");

        String destination = "/topic/verify-transaction/" + phoneNumber;

        try {
            messagingTemplate.convertAndSend(destination, payload);
            System.out.println("Mensaje enviado al destino: " + destination);

            boolean hasSubscribers = simpUserRegistry.getUsers().stream()
                    .flatMap(user -> user.getSessions().stream())
                    .flatMap(session -> session.getSubscriptions().stream())
                    .anyMatch(subscription -> destination.equals(subscription.getDestination()));

            if (hasSubscribers) {
                System.out.println("Hay suscriptores activos para el destino: " + destination);
            } else {
                System.out.println("No hay suscriptores activos para el destino: " + destination);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar el mensaje al frontend para el número de teléfono: " + phoneNumber);
            e.printStackTrace();
        }
    }

    @MessageMapping("/verifyTransaction")
    public void verifyTransaction(@Payload Map<String, String> verificationData) {
        String phoneNumber = verificationData.get("phoneNumber");
        Long pin = Long.valueOf(verificationData.get("pin"));
        String refNumber = verificationData.get("refNumber");
    }
}
