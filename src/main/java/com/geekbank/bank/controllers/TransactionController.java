package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

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
}
