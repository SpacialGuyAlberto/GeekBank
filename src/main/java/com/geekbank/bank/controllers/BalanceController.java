package com.geekbank.bank.controllers;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.*;
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

        // Crear una transacción para la compra de balance
        Transaction transaction;
        try {
            transaction = transactionService.createTransaction(
                    user,
                    balanceRequest.getGuestId(),
                    null,
                    balanceRequest.getId(),
                    balanceRequest.getAmount(),
                    TransactionType.BALANCE_PURCHASE, // Necesitarás agregar este tipo
                    "Compra de balance",
                    balanceRequest.getPhoneNumber(),
                    null// Sin productos asociados
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating transaction: " + e.getMessage());
        }

        // Aquí puedes integrar el servicio de pago (por ejemplo, Tigo)
        // Enviar solicitud de pago, etc.

        String responseMessage = "Balance purchase initiated. Transaction number: " + transaction.getTransactionNumber();
        return ResponseEntity.ok(responseMessage);
    }
}
