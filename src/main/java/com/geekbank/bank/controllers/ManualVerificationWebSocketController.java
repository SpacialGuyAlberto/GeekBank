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

    public void sendManualVerificationTransaction(Transaction transaction) {
        String destination = "/topic/manual-verifications";

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


        public String getTransactionNumber() {
            return transactionNumber;
        }

        public void setTransactionNumber(String transactionNumber) {
            this.transactionNumber = transactionNumber;
        }

        public double getAmountUsd() {
            return amountUsd;
        }

        public void setAmountUsd(double amountUsd) {
            this.amountUsd = amountUsd;
        }

        public double getAmountHnl() {
            return amountHnl;
        }

        public void setAmountHnl(double amountHnl) {
            this.amountHnl = amountHnl;
        }

        public double getExchangeRate() {
            return exchangeRate;
        }

        public void setExchangeRate(double exchangeRate) {
            this.exchangeRate = exchangeRate;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getUserOrGuest() {
            return userOrGuest;
        }

        public void setUserOrGuest(String userOrGuest) {
            this.userOrGuest = userOrGuest;
        }

        public List<TransactionProductDto> getProducts() {
            return products;
        }

        public void setProducts(List<TransactionProductDto> products) {
            this.products = products;
        }

        public static class TransactionProductDto {
            private Long productId;
            private int quantity;
            private double price;

            public TransactionProductDto(TransactionProduct product) {
                this.productId = product.getProductId();
                this.quantity = product.getQuantity();

            }

            public Long getProductId() {
                return productId;
            }

            public void setProductId(Long productId) {
                this.productId = productId;
            }

            public int getQuantity() {
                return quantity;
            }

            public void setQuantity(int quantity) {
                this.quantity = quantity;
            }

            public double getPrice() {
                return price;
            }

            public void setPrice(double price) {
                this.price = price;
            }
        }
    }
}
