package com.geekbank.bank.services;

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

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Transaction createTransaction(Long userId, double amount, TransactionType type, String description, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);


        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber()); // Lógica para generar un número único
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setPhoneNumber(phoneNumber);

        Transaction savedTransaction = transactionRepository.save(transaction);

//        savedTransaction.setStatus(TransactionStatus.COMPLETED);
        return savedTransaction;
    }

    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);
    }

    @Transactional(rollbackFor = Exception.class)
    public void performTransactionWithRollback(Long userId, double amount, String phoneNumber) throws Exception {
        try {
            // Crear la transacción
            createTransaction(userId, amount, TransactionType.PURCHASE, "Compra de producto", phoneNumber);
            // Lógica adicional que puede fallar

            // Simulación de error
            if (amount > 1000) {
                throw new Exception("Monto de la transacción excede el límite permitido.");
            }
        } catch (Exception e) {
            throw new Exception("Error durante la transacción: " + e.getMessage());
        }
    }

    private String generateTransactionNumber() {
        return "TX-" + System.currentTimeMillis();
    }
}
