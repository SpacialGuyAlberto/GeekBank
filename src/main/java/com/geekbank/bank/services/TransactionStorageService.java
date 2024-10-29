package com.geekbank.bank.services;
import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
@Service
public class TransactionStorageService {

    private final ConcurrentHashMap<String, List<Transaction>> pendingTransactions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> smsReferenceNumbers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> amountReceivedMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Transaction> manualTransactionQueue = new ConcurrentLinkedQueue<>();

    public synchronized void storePendingTransaction(Transaction transaction) {
        pendingTransactions.computeIfAbsent(transaction.getPhoneNumber(), k -> new ArrayList<>()).add(transaction);
        System.out.println("Stored transaction for phone number: " + transaction.getPhoneNumber() + " | Transaction Number: " + transaction.getTransactionNumber());
    }

    public synchronized void removeTransaction(String phoneNumber, String transactionNumber) {
        List<Transaction> transactions = pendingTransactions.get(phoneNumber);
        if (transactions != null) {
            transactions.removeIf(tx -> tx.getTransactionNumber().equals(transactionNumber));
            if (transactions.isEmpty()) {
                pendingTransactions.remove(phoneNumber);
            }
            System.out.println("Removed transaction " + transactionNumber + " for phone number: " + phoneNumber);
        }
    }

    public synchronized void removeTransactionById(Long transactionId) {
        System.out.println("Attempting to remove transaction by ID: " + transactionId);

        pendingTransactions.forEach((phoneNumber, transactions) -> {
            transactions.removeIf(transaction -> transaction.getId().equals(transactionId));
            if (transactions.isEmpty()) {
                pendingTransactions.remove(phoneNumber);
            }
        });
    }

    public List<Transaction> getPendingTransactions(String phoneNumber) {
        return pendingTransactions.getOrDefault(phoneNumber, Collections.emptyList());
    }

    public List<Transaction> findPendingTransactionsForPhoneNumberAndAmount(String phoneNumber, double amountReceived) {
        List<Transaction> transactions = pendingTransactions.getOrDefault(phoneNumber, Collections.emptyList());
        return transactions.stream()
                .filter(transaction -> transaction.getAmountHnl() <= amountReceived)
                .collect(Collectors.toList());
    }

    public List<Transaction> findPendingTransactionsForPhoneNumber(String phoneNumber) {
        List<Transaction> transactions = pendingTransactions.getOrDefault(phoneNumber, Collections.emptyList());
        return transactions.stream().collect(Collectors.toList());
    }

    public synchronized void storeSmsReferenceNumber(String phoneNumber, String referenceNumber) {
        smsReferenceNumbers.put(phoneNumber, referenceNumber);
        System.out.println("Stored SMS Reference Number for phone number: " + phoneNumber + " | Reference Number: " + referenceNumber);
    }

    public synchronized String getSmsReferenceNumber(String phoneNumber) {
        return smsReferenceNumbers.get(phoneNumber);
    }

    public synchronized void removeSmsReferenceNumber(String phoneNumber) {
        smsReferenceNumbers.remove(phoneNumber);
        System.out.println("Removed SMS Reference Number for phone number: " + phoneNumber);
    }

    public synchronized void storeAmountReceived(String phoneNumber, double amountReceived) {
        amountReceivedMap.put(phoneNumber, amountReceived);
        System.out.println("Stored amount received for phone number: " + phoneNumber + " | Amount: " + amountReceived);
    }

    public synchronized Double getAmountReceived(String phoneNumber) {
        return amountReceivedMap.get(phoneNumber);
    }

    public synchronized void removeAmountReceived(String phoneNumber) {
        amountReceivedMap.remove(phoneNumber);
        System.out.println("Removed amount received for phone number: " + phoneNumber);
    }

    public void addManualTransaction(Transaction transaction) {
        manualTransactionQueue.add(transaction);
        System.out.println("Added manual transaction to queue: " + transaction.getTransactionNumber());
    }

    // Método para obtener y remover la siguiente transacción manual de la cola
    public Transaction pollManualTransaction() {
        return manualTransactionQueue.poll();
    }

    // Método para obtener todas las transacciones manuales en espera
    public List<Transaction> getAllManualTransactions() {
        return new ArrayList<>(manualTransactionQueue);
    }

    // Método para obtener una transacción manual por número de transacción
    public Transaction getManualTransactionByNumber(String transactionNumber) {
        return manualTransactionQueue.stream()
                .filter(tx -> tx.getTransactionNumber().equals(transactionNumber))
                .findFirst()
                .orElse(null);
    }

    // Método para remover una transacción manual de la cola
    public void removeManualTransaction(String transactionNumber) {
        manualTransactionQueue.removeIf(tx -> tx.getTransactionNumber().equals(transactionNumber));
        System.out.println("Removed manual transaction from queue: " + transactionNumber);
    }
}
