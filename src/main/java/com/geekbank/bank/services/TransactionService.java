package com.geekbank.bank.services;

import com.geekbank.bank.controllers.TransactionWebSocketController;
import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Transactional
    public Transaction createTransaction(User user, double amount, TransactionType type, String description, String phoneNumber, List<OrderRequest.Product> products) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription(description);
        transaction.setPhoneNumber(phoneNumber);
        transaction.setStatus(TransactionStatus.PENDING);

        // Limitar la cantidad de productos a 10
        if (products.size() > 10) {
            throw new IllegalArgumentException("No se pueden agregar más de 10 productos por transacción.");
        }

        // Crear TransactionProduct para cada producto en la solicitud
        List<TransactionProduct> transactionProducts = products.stream().map(productRequest -> {
            Long productId = (long) productRequest.getKinguinId();

            // Aquí no verificamos la existencia del producto
            // Solo almacenamos el productId
            TransactionProduct transactionProduct = new TransactionProduct();
            transactionProduct.setTransaction(transaction);
            transactionProduct.setProductId(productId);
            transactionProduct.setQuantity(productRequest.getQty());

            return transactionProduct;
        }).collect(Collectors.toList());

        transaction.setProducts(transactionProducts);

        Transaction savedTransaction = transactionRepository.save(transaction);
        transactionStorageService.storePendingTransaction(savedTransaction);
        // Aquí puedes agregar lógica adicional, como almacenar la transacción pendiente
        return savedTransaction;
    }



    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);

        webSocketController.notifyTransactionUpdate(transaction.getPhoneNumber(), newStatus.name());
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


}
