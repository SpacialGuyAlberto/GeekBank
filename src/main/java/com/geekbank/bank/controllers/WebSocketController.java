package com.geekbank.bank.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.geekbank.bank.services.TelegramListener;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTransactionStatus(String phoneNumber, String status, String message, String transactionId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("message", message);
        payload.put("transactionId", transactionId);
        messagingTemplate.convertAndSend("/topic/transaction-status/" + phoneNumber, payload);
        System.out.println("Transaction update sent to WebSocket for phone number: " + phoneNumber + " | Status: " + status);
    }


    public void requestRefNumberAndTempPin(String phoneNumber) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Por favor, ingrese su PIN y el número de referencia de su pago para verificar su transacción.");
        messagingTemplate.convertAndSend("/topic/verify-transaction/" + phoneNumber, payload);
        System.out.println("Sent verification request to frontend for phone number: " + phoneNumber);
    }

    @MessageMapping("/verifyTransaction")
    public void verifyTransaction(@Payload Map<String, String> verificationData) {
        String phoneNumber = verificationData.get("phoneNumber");
        Long pin = Long.valueOf(verificationData.get("pin"));
        String refNumber = verificationData.get("refNumber");

        

    }
}
