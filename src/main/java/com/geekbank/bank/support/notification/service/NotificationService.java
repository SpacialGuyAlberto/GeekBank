package com.geekbank.bank.support.notification.service;

import com.geekbank.bank.transaction.constants.TransactionStatus;
import com.geekbank.bank.user.admin.controller.WebSocketController;
import com.geekbank.bank.transaction.controller.TransactionWebSocketController;
import com.geekbank.bank.order.manual.controller.ManualVerificationWebSocketController;
import com.geekbank.bank.transaction.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private TransactionWebSocketController transactionWebSocketController;

    @Autowired
    private ManualVerificationWebSocketController manualVerificationWebSocketController;

    public void notifyTransactionStatus(String phoneNumber, String status, String message, String transactionNumber) {
        webSocketController.notifyTransactionStatus(phoneNumber, status, message, transactionNumber);
    }

    public void sendTransactionStatus(TransactionStatus status) {
        webSocketController.sendTransactionStatus(status);
    }

    public void sendManualVerificationTransaction(Transaction transaction) {
        manualVerificationWebSocketController.sendManualVerificationTransaction(transaction);
    }
}
