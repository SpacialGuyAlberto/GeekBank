package com.geekbank.bank.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTransactionUpdate(String phoneNumber, String status, String failureReason) {
        // Crear un objeto que contenga el estado y la razón del fallo
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        if (failureReason != null) {
            payload.put("reason", failureReason);
        }
        messagingTemplate.convertAndSend("/topic/transaction-status/" + phoneNumber, payload);
    }
}
