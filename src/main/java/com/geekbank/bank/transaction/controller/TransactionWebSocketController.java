package com.geekbank.bank.transaction.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.geekbank.bank.transaction.model.Transaction;

@Controller
public class TransactionWebSocketController {
    @MessageMapping("/transaction-status")
    @SendTo("/topic/transactions")
    public Transaction sendTransactionStatus(Transaction transaction) {
        return transaction;
    }
}
