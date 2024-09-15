package com.geekbank.bank.services;

import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import com.geekbank.bank.models.TransactionType;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geekbank.bank.repositories.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionStorageService transactionStorageService;
    @Autowired
    private WebSocketController webSocketController;


    @Transactional
    public Transaction createTransaction(User user, double amount, TransactionType type, String description, String phoneNumber) {
        Transaction transaction = new Transaction();
        // Set all necessary fields
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription(description);
        transaction.setPhoneNumber(phoneNumber);
        transaction.setAccount(null);
        // Ensure the status is set
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction savedTransaction = transactionRepository.save(transaction);
        // Save the transaction to the database
        transactionStorageService.storePendingTransaction(savedTransaction);
        return savedTransaction;
    }

    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);

//        webSocketController.notifyTransactionUpdate(transaction.getPhoneNumber(), newStatus.name());
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
}
