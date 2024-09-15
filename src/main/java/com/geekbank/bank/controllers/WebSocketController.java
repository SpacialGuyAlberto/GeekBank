package com.geekbank.bank.controllers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyTransactionUpdate(String phoneNumber, String status) {
        messagingTemplate.convertAndSend("/topic/transaction-status/" + phoneNumber, status);
    }
}
