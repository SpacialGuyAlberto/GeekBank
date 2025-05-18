package com.geekbank.bank.balance.controller;

import com.geekbank.bank.balance.dto.BalancePurchaseRequest;
import com.geekbank.bank.transaction.constants.TransactionType;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.transaction.service.TransactionService;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseBalance(@RequestBody BalancePurchaseRequest balanceRequest) {
        User user = null;

        if (balanceRequest.getUserId() != null){
            user = userRepository.findById(balanceRequest.getUserId())
                    .orElse(null);
        }

        balanceRequest.setId();
        Transaction transaction;
        try {
            transaction = transactionService.createTransaction(
                    user,
                    balanceRequest.getGuestId(),
                    null,
                    balanceRequest.getId(),
                    balanceRequest.getAmount(),
                    TransactionType.BALANCE_PURCHASE,
                    "Compra de balance",
                    balanceRequest.getPhoneNumber(),
                    null,
                    false
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating transaction: " + e.getMessage());
        }

        String responseMessage = "Balance purchase initiated. Transaction number: " + transaction.getTransactionNumber();
        return ResponseEntity.ok(responseMessage);
    }
}
