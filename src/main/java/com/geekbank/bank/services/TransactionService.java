package com.geekbank.bank.services;

import com.geekbank.bank.controllers.TransactionWebSocketController;
import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geekbank.bank.repositories.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionStorageService transactionStorageService;
    @Autowired
    private WebSocketController webSocketController;
    @Autowired
    private GiftCardRepository giftCardRepository;
    @Autowired
    private TransactionWebSocketController transactionWebSocketController;

    @Autowired
    private UserRepository userRepository;

    private static final long EXPIRATION_MINUTES = 5;


    @Transactional
    public Transaction createTransaction(User user, String guestId, String orderRequestNumber, double amount, TransactionType type, String description, String phoneNumber, List<OrderRequest.Product> products) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);

        if (user != null) {
            transaction.setUser(user);
        } else if (guestId != null && !guestId.isEmpty()) {
            transaction.setGuestId(guestId);
        } else {
            throw new IllegalArgumentException("Either userId or guestId must be provided");
        }

        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription(description);
        transaction.setPhoneNumber(phoneNumber);

        transaction.setOrderRequestNumber(orderRequestNumber);

        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));

        if (products != null && products.size() > 10) {
            throw new IllegalArgumentException("No se pueden agregar más de 10 productos por transacción.");
        }

        List<TransactionProduct> transactionProducts = products.stream().map(productRequest -> {
            Long productId = (long) productRequest.getKinguinId();
            TransactionProduct transactionProduct = new TransactionProduct();
            transactionProduct.setTransaction(transaction);
            transactionProduct.setProductId(productId);
            transactionProduct.setQuantity(productRequest.getQty());

            return transactionProduct;
        }).collect(Collectors.toList());

        transaction.setProducts(transactionProducts);

        Transaction savedTransaction = transactionRepository.save(transaction);
        transactionStorageService.storePendingTransaction(savedTransaction);
        return savedTransaction;
    }


    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus, String failureReason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaction.getStatus() == TransactionStatus.CANCELLED){
            throw new RuntimeException("No se puede actualizar una transacción cancelada.");
        }
        if (transaction.getStatus() == TransactionStatus.EXPIRED){
            throw new RuntimeException("No se puede actualizar una transacción expirada.");
        }

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);

        // Notificar al frontend con la razón del fallo si existe
        if (failureReason != null && !failureReason.isEmpty()) {
            webSocketController.notifyTransactionUpdate(transaction.getPhoneNumber(), newStatus.name(), failureReason);
        } else {
            webSocketController.notifyTransactionUpdate(transaction.getPhoneNumber(), newStatus.name(), null);
        }
    }


    public List<Transaction> findPendingTransactionsByPhoneNumber(String phoneNumber) {
        return transactionRepository.findByStatusAndPhoneNumber(TransactionStatus.PENDING, phoneNumber);
    }
    public Transaction findByTransactionNumber(String transactionNumber){
        return transactionRepository.findByTransactionNumber(transactionNumber);
    }

    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void performTransactionWithRollback(User user, double amount, String phoneNumber) throws Exception {
//        try {
//            // Crear la transacción
//            createTransaction(user, amount, TransactionType.PURCHASE, "Compra de producto", phoneNumber);
//            // Lógica adicional que puede fallar
//
//            // Simulación de error
//            if (amount > 1000) {
//                throw new Exception("Monto de la transacción excede el límite permitido.");
//            }
//        } catch (Exception e) {
//            throw new Exception("Error durante la transacción: " + e.getMessage());
//        }
//    }

    public List<Transaction> getTransactionByUserId(Long userId) { return transactionRepository.findByUserId(userId);}
    private String generateTransactionNumber() {
        return "TX-" + System.currentTimeMillis();
    }

    public List<Transaction> getTransactionsByUserIdAndTimestamp(Long userId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetweenAndUserId(start, end, userId);
    }

    public List<Transaction> getTransactionsByTimestamp(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetween(start, end);
    }

    @Scheduled(fixedRate = 360000)
    @Transactional
    public void expireTransaction(){
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> pendingTransactions = transactionRepository.findByStatusAndTimestampBefore(TransactionStatus.PENDING, now);

        for (Transaction transaction : pendingTransactions) {
            transaction.setStatus(TransactionStatus.EXPIRED);
            transactionRepository.save(transaction);

            transactionStorageService.removeTransactionById(transaction.getId());

            System.out.println("Transaction expired: " + transaction.getTransactionNumber());
        }
    }


}
