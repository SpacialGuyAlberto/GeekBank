package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.services.OrderRequestStorageService;
import com.geekbank.bank.services.TransactionService;
import com.geekbank.bank.services.TransactionStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPendingTransactionsByPhoneNumber(@RequestParam String phoneNumber) {
        List<Transaction> transactions = transactionService.findPendingTransactionsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping()
    public List<Transaction> getTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{userId}")
    public List<Transaction> getTransactionsById(@PathVariable long userId){
        return transactionService.getTransactionByUserId(userId);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getTransactionsByUserIdAndTimestamp(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = transactionService.getTransactionsByUserIdAndTimestamp(userId, start, end);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/cancel/{transactionId}/{orderRequestId}")
    public ResponseEntity<Transaction> cancelRunningTransaction(
            @PathVariable String transactionId,
            @PathVariable String orderRequestId
    ) {
        Transaction transaction = transactionService.findByTransactionNumber(transactionId);
        transactionStorageService.removeTransactionById(transaction.getId());
        transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
        orderRequestStorageService.removeOrderRequestById(orderRequestId);

        return ResponseEntity.ok(transaction);
    }

}
