package com.geekbank.bank.services;

import com.geekbank.bank.controllers.ManualVerificationWebSocketController;
import com.geekbank.bank.controllers.TransactionWebSocketController;
import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.repositories.GiftCardRepository;
import com.geekbank.bank.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geekbank.bank.repositories.TransactionRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
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
    @Autowired
    private OrderRequestStorageService orderRequestStorageService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CurrencyService currencyService; // Inyectar Currenc

    private static final long EXPIRATION_MINUTES = 5;
    private PriorityBlockingQueue<Transaction> manualVerificationQueue;
    @Autowired
    private ManualVerificationWebSocketController manualVerificationWebSocketController;

    private static Long generatePin() {
        Random random = new Random();
        int pin = random.nextInt(9999) + 1;
        String formattedPin = String.format("%04d", pin); // Asegura que tenga cuatro dígitos
        return Long.parseLong(formattedPin); // Convierte a Long antes de devolverlo
    }

    @Transactional
    public Transaction createTransaction(User user, String guestId, Long gameUserId, String orderRequestNumber, double amountUSD, TransactionType type, String description, String phoneNumber, List<OrderRequest.Product> products, Boolean isManual) {
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();

        double amountHNL = currencyService.convertUsdToHnl(amountUSD, exchangeRate);

        Transaction transaction = new Transaction();
        transaction.setAmountUsd(amountUSD);
        transaction.setAmountHnl(amountHNL);
        transaction.setExchangeRate(exchangeRate);

        if (user != null) {
            transaction.setUser(user);
        } else if (guestId != null && !guestId.isEmpty()) {
            transaction.setGuestId(guestId);
        } else {
            throw new IllegalArgumentException("Either userId or guestId must be provided");
        }

        if (gameUserId != null){
            transaction.setGameUserId(gameUserId);
        }

        if (isManual != null){
            transaction.setManual(isManual);
            transaction.setExpiresAt(LocalDateTime.now().plusMinutes(isManual ? 600 : EXPIRATION_MINUTES));
        } else {
            transaction.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
            transaction.setManual(false);
        }

        System.out.println("CREATING TRANSACTION: " + isManual);

        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription(description);
        transaction.setPhoneNumber(phoneNumber);
        transaction.setOrderRequestNumber(orderRequestNumber);
        transaction.setTempPin(generatePin());
        transaction.setStatus(TransactionStatus.PENDING);

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
    public void verifyTransaction(String phoneNumber, Long pin, String refNumber) {
        try {
            Transaction matchingTransaction = getMatchingTransaction(phoneNumber, pin);
            validateReferenceNumber(phoneNumber, refNumber);

            Double amountReceived = transactionStorageService.getAmountReceived(phoneNumber);
            if (amountReceived == null) {
                throw new RuntimeException("No se encontró el monto recibido para este número de teléfono.");
            }

            Optional<Transaction> optionalTransactionInDB = Optional.ofNullable(transactionRepository.findByTransactionNumber(matchingTransaction.getTransactionNumber()));
            if (!optionalTransactionInDB.isPresent()) {
                throw new RuntimeException("Transacción no encontrada en la base de datos.");
            }
            Transaction transactionInDB = optionalTransactionInDB.get();

            if (amountReceived < transactionInDB.getAmountHnl()) {
                updateTransactionStatus(transactionInDB.getId(), TransactionStatus.FAILED, "Monto recibido insuficiente.");
                throw new RuntimeException("Monto recibido insuficiente.");
            }

            if (transactionInDB.getManual()) {
                // Actualizar el estado de la transacción a AWAITING_MANUAL_PROCESSING
                updateTransactionStatus(transactionInDB.getId(), TransactionStatus.AWAITING_MANUAL_PROCESSING, null);

                // Agregar la transacción a la lista de espera para procesamiento manual
                transactionStorageService.addManualTransaction(transactionInDB);

                // Notificar al frontend que la transacción está pendiente de procesamiento manual
                webSocketController.notifyTransactionStatus(phoneNumber, TransactionStatus.AWAITING_MANUAL_PROCESSING.name(), "Pendiente de procesamiento manual.", transactionInDB.getTransactionNumber());

                System.out.println("Transacción manual agregada a la lista de espera. Transaction ID: " + transactionInDB.getTransactionNumber());
            } else {
                // Procesar la transacción normalmente
                processTransaction(transactionInDB, amountReceived);

                // Limpiar datos temporales y notificar al frontend
                cleanUpAfterSuccess(phoneNumber, matchingTransaction);
            }

        } catch (Exception e) {
            // Manejo de errores y notificación al frontend
            handleVerificationError(phoneNumber, e.getMessage());
        }
    }


    private Transaction getMatchingTransaction(String phoneNumber, Long pin) {
        List<Transaction> pendingTransactions = transactionStorageService.getPendingTransactions(phoneNumber);

        if (pendingTransactions.isEmpty()) {
            throw new RuntimeException("No hay transacciones pendientes para este número de teléfono.");
        }

        // Encontrar la transacción que coincide con el PIN ingresado
        Optional<Transaction> optionalTransaction = pendingTransactions.stream()
                .filter(tx -> tx.getTempPin() != null && tx.getTempPin().equals(pin))
                .findFirst();

        if (!optionalTransaction.isPresent()) {
            throw new RuntimeException("No se encontró una transacción pendiente con el PIN proporcionado.");
        }

        return optionalTransaction.get();
    }

    /**
            * Procesa la transacción según su tipo.
            *
            * @param transactionInDB  Transacción obtenida de la base de datos.
            * @param amountReceivedHNL Monto recibido en HNL.
     */
    private void processTransaction(Transaction transactionInDB, double amountReceivedHNL) {
        // Verificar que el monto recibido es suficiente
        if (amountReceivedHNL < transactionInDB.getAmountHnl()) {
            updateTransactionStatus(transactionInDB.getId(), TransactionStatus.FAILED, "Monto recibido insuficiente.");
            throw new RuntimeException("Monto recibido insuficiente.");
        }

        if (transactionInDB.getType() == TransactionType.BALANCE_PURCHASE) {
            processBalancePurchase(transactionInDB);
        } else if (transactionInDB.getType() == TransactionType.PURCHASE) {
            processProductPurchase(transactionInDB);
        } else {
            System.out.println("Tipo de transacción desconocido. Se omite el procesamiento.");
        }
    }

    /**
            * Procesa una compra de balance, actualizando el saldo del usuario.
     *
             * @param transactionInDB Transacción a procesar.
            */
    private void processBalancePurchase(Transaction transactionInDB) {
        User user = transactionInDB.getUser();
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado para la transacción.");
        }
        Account account = user.getAccount();
        if (account == null) {
            throw new RuntimeException("Cuenta no encontrada para el usuario.");
        }

        account.setBalance(account.getBalance() + transactionInDB.getAmountUsd());
        accountRepository.save(account);

        updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED, null);
        System.out.println("Balance actualizado para el usuario: " + user.getEmail());
    }

    private void validateReferenceNumber(String phoneNumber, String refNumber) {
        String storedRefNumber = transactionStorageService.getSmsReferenceNumber(phoneNumber);

        if (storedRefNumber == null) {
            throw new RuntimeException("No se encontró un número de referencia de SMS para este número de teléfono.");
        }

        if (!storedRefNumber.equals(refNumber)) {
            throw new RuntimeException("El número de referencia no coincide con el recibido en el mensaje de texto.");
        }
    }

    private void processProductPurchase(Transaction transactionInDB) {
        updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED, null);
        System.out.println("Transacción de compra de producto completada. Transaction ID: " + transactionInDB.getTransactionNumber());
    }

    private void cleanUpAfterSuccess(String phoneNumber, Transaction matchingTransaction) {
        transactionStorageService.removeTransaction(phoneNumber, matchingTransaction.getTransactionNumber());
        transactionStorageService.removeSmsReferenceNumber(phoneNumber);
        transactionStorageService.removeAmountReceived(phoneNumber);
        orderRequestStorageService.removeOrderRequest(phoneNumber);
        webSocketController.notifyTransactionStatus(phoneNumber, "COMPLETED", "Your transaction was successfully completed.", matchingTransaction.getTransactionNumber());
    }

    private void handleVerificationError(String phoneNumber, String errorMessage) {
        System.err.println("Error al verificar la transacción: " + errorMessage);
        webSocketController.notifyTransactionStatus(phoneNumber, "FAILED", errorMessage, null);
    }

    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus, String message) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (!isUpdatableStatus(transaction.getStatus())) {
            throw new RuntimeException("No se puede actualizar una transacción en estado " + transaction.getStatus() + ".");
        }

        transaction.setStatus(newStatus);
        transactionRepository.save(transaction);

        webSocketController.notifyTransactionStatus(
                transaction.getPhoneNumber(),
                newStatus.name(),
                message,
                transaction.getTransactionNumber()
        );
    }
    private boolean isUpdatableStatus(TransactionStatus currentStatus) {
        return currentStatus != TransactionStatus.CANCELLED && currentStatus != TransactionStatus.EXPIRED;
    }


    public void processManualTransaction(Transaction transaction) {
        manualVerificationQueue.offer(transaction);
        manualVerificationWebSocketController.sendManualVerificationTransaction(transaction);
    }

    public List<Transaction> findTransactionByGameUserId(Long gameUserId){
        return transactionRepository.findByGameUserId(gameUserId);
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

    @Transactional
    public void approveManualTransaction(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber);
        if (transaction == null) {
            throw new RuntimeException("Transacción no encontrada.");
        }
        if (transaction.getStatus() != TransactionStatus.AWAITING_MANUAL_PROCESSING) {
            throw new RuntimeException("La transacción no está en estado de verificación manual.");
        }

        // Procesar la transacción (similar a processTransaction)
        processTransaction(transaction, transaction.getAmountHnl());

        // Actualizar el estado a COMPLETED
        updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED, "Transacción aprobada manualmente.");
    };

    @Transactional
    public void rejectManualTransaction(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber);
        if (transaction == null) {
            throw new RuntimeException("Transacción no encontrada.");
        }
        if (transaction.getStatus() != TransactionStatus.AWAITING_MANUAL_PROCESSING) {
            throw new RuntimeException("La transacción no está en estado de verificación manual.");
        }

        // Actualizar el estado a FAILED
        updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED, "Transacción rechazada manualmente.");
    }
}
