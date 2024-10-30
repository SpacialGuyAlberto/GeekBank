// src/main/java/com/geekbank/bank/controllers/ManualVerificationWebSocketController.java

package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ManualVerificationWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una transacción para verificación manual al frontend.
     *
     * @param transaction Transacción a enviar.
     */
    public void sendManualVerificationTransaction(Transaction transaction) {
        String destination = "/topic/manual-verifications";

        // Crear un DTO para la transacción
        ManualVerificationTransactionDto dto = new ManualVerificationTransactionDto(
                transaction.getTransactionNumber(),
                transaction.getAmountUsd(),
                transaction.getAmountHnl(),
                transaction.getExchangeRate(),
                transaction.getTimestamp(),
                transaction.getPhoneNumber(),
                transaction.getUser() != null ? transaction.getUser().getEmail() : transaction.getGuestId(),
                transaction.getProducts()
        );

        // Enviar el DTO
        messagingTemplate.convertAndSend(destination, dto);
    }

    /**
     * Envía la lista completa de transacciones manuales para verificación al frontend.
     *
     * @param transactions Lista de transacciones.
     */
    public void sendManualVerificationQueue(List<Transaction> transactions) {
        String destination = "/topic/manual-verifications-queue";

        List<ManualVerificationTransactionDto> dtoList = transactions.stream()
                .sorted((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()))
                .map(tx -> new ManualVerificationTransactionDto(
                        tx.getTransactionNumber(),
                        tx.getAmountUsd(),
                        tx.getAmountHnl(),
                        tx.getExchangeRate(),
                        tx.getTimestamp(),
                        tx.getPhoneNumber(),
                        tx.getUser() != null ? tx.getUser().getEmail() : tx.getGuestId(),
                        tx.getProducts()
                ))
                .collect(Collectors.toList());

        messagingTemplate.convertAndSend(destination, dtoList);
    }

    // DTO Classes

    public static class ManualVerificationTransactionDto {
        private String transactionNumber;
        private double amountUsd;
        private double amountHnl;
        private double exchangeRate;
        private LocalDateTime timestamp;
        private String phoneNumber;
        private String userOrGuest;
        private List<TransactionProductDto> products;

        public ManualVerificationTransactionDto(String transactionNumber, double amountUsd, double amountHnl, double exchangeRate, LocalDateTime timestamp, String phoneNumber, String userOrGuest, List<TransactionProduct> products) {
            this.transactionNumber = transactionNumber;
            this.amountUsd = amountUsd;
            this.amountHnl = amountHnl;
            this.exchangeRate = exchangeRate;
            this.timestamp = timestamp;
            this.phoneNumber = phoneNumber;
            this.userOrGuest = userOrGuest;
            this.products = products.stream()
                    .map(TransactionProductDto::new)
                    .collect(Collectors.toList());
        }

        // Getters y Setters

        // ...

        public static class TransactionProductDto {
            private Long productId;
            private int quantity;
            private double price;

            public TransactionProductDto(TransactionProduct product) {
                this.productId = product.getProductId();
                this.quantity = product.getQuantity();
            }

            // Getters y Setters

            // ...
        }
    }
}
