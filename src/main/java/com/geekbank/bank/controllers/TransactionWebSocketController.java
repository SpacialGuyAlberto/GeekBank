package com.geekbank.bank.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.geekbank.bank.models.Transaction;

@Controller
public class TransactionWebSocketController {
    @MessageMapping("/transaction-status")
    @SendTo("/topic/transactions")
    public Transaction sendTransactionStatus(Transaction transaction) {
        return transaction;
    }
}
