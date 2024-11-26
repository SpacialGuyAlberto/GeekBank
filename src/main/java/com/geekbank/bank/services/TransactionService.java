package com.geekbank.bank.services;

import com.geekbank.bank.controllers.ManualVerificationWebSocketController;
import com.geekbank.bank.controllers.TransactionWebSocketController;
import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.dto.UnmatchedPaymentResponseDto;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.geekbank.bank.exceptions.ResourceNotFoundException;
import com.geekbank.bank.exceptions.InsufficientBalanceException;
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
    private SmsMessageRepository smsMessageRepository;
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

    private static final long EXPIRATION_MINUTES = 3;
    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;

    @Autowired
    private ManualVerificationWebSocketController manualVerificationWebSocketController;

    private PriorityBlockingQueue<Transaction> manualVerificationQueue = new PriorityBlockingQueue<>(
            100,
            Comparator.comparing(Transaction::getTimestamp)
    );


    private static Long generatePin() {
        Random random = new Random();
        int pin = random.nextInt(9000) + 1000; // Genera un número entre 1000 y 9999
        return (long) pin;
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
        updateTransactionStatus(savedTransaction.getId(), TransactionStatus.PENDING, null);
        return savedTransaction;
    }

    @Transactional
    public Transaction purchaseWithBalance(Long userId, String orderRequestNumber, List<OrderRequest.Product> products, String phoneNumber) {
        // 1. Recuperar el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // 2. Calcular el costo total en USD
        double totalAmountUsd = products.stream()
                .mapToDouble(product -> product.getPrice() * product.getQty())
                .sum();

        // 3. Obtener la tasa de cambio USD a HNL
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();

        // 4. Convertir el monto a HNL
        double totalAmountHnl = currencyService.convertUsdToHnl(totalAmountUsd, exchangeRate);

        // 5. Recuperar la cuenta del usuario
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para el usuario con ID: " + userId));

        // 6. Verificar si el saldo es suficiente
        if (account.getBalance() < totalAmountUsd) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar la compra.");
        }

        // 7. Deducir el monto del balance
        account.setBalance(account.getBalance() - totalAmountUsd);
        accountRepository.save(account);

        // 8. Crear una nueva transacción
        Transaction transaction = new Transaction();
        transaction.setAmountUsd(totalAmountUsd);
        transaction.setAmountHnl(totalAmountHnl);
        transaction.setExchangeRate(exchangeRate);
        transaction.setUser(user);
        transaction.setManual(false);
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        transaction.setType(TransactionType.BALANCE_PURCHASE);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription("Compra con balance");
        transaction.setPhoneNumber(phoneNumber);
        transaction.setOrderRequestNumber(orderRequestNumber);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTempPin(null);

        // 9. Asociar los productos a la transacción
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

        // 10. Guardar la transacción en la base de datos
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 11. Notificar al usuario vía WebSocket
        webSocketController.notifyTransactionStatus(
                phoneNumber,
                TransactionStatus.COMPLETED.name(),
                "Compra realizada exitosamente utilizando tu balance.",
                savedTransaction.getTransactionNumber()
        );

        return savedTransaction;
    }

    public ResponseEntity<UnmatchedPaymentResponseDto> verifyUnmatchedPaymentAmount(String referenceNumber, String phoneNumber, double expectedAmount) {
        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumberAndPhoneNumber(referenceNumber, phoneNumber);

        if (unmatchedPayment == null) {
            return ResponseEntity.status(404).body(null);
        }

        double receivedAmount = unmatchedPayment.getAmountReceived();
        double difference = receivedAmount - expectedAmount;
        String message;
        List<String> options;

        if (difference == 0) {
            message = "El pago coincide con el monto esperado.";
            options = null;  // No hay opciones adicionales cuando no hay diferencia
        } else if (difference > 0) {
            message = "Hay una diferencia en el monto del pago.";
            options = Arrays.asList(
                    "Apply the difference as a balance",
                    difference > 1 ? "Return the difference" : "No se puede devolver la diferencia (debe ser mayor a 1)",
                    "Adjust the payment to match the expected amount"
            );
        } else {
            message = "El monto recibido es menor al monto esperado.";
            options = Arrays.asList(
                    "Quiero mi dinero de nuevo",
                    "Combinar este pago con otro nuevo pago"
            );
        }

        UnmatchedPaymentResponseDto response = new UnmatchedPaymentResponseDto(
                unmatchedPayment,
                receivedAmount,
                expectedAmount,
                difference,
                message,
                options
        );

        return ResponseEntity.ok(response);
    }




    @Transactional
    public Transaction verifyPaymentAndCreateOrder(String refNumber, String phoneNumber, OrderRequest orderRequest) {
        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumberAndPhoneNumber(refNumber, phoneNumber);

        if (unmatchedPayment == null) {
            throw new RuntimeException("Pago no encontrado con el número de referencia y teléfono proporcionados.");
        }

        double amountReceived = unmatchedPayment.getAmountReceived();
        double orderAmount = orderRequest.getAmount();

        if (amountReceived < orderAmount) {
            throw new RuntimeException("El monto del pago es insuficiente para esta orden.");
        }

        TransactionType transactionType = TransactionType.PURCHASE;
        if (orderRequest.getProducts() != null && !orderRequest.getProducts().isEmpty()) {
            OrderRequest.Product firstProduct = orderRequest.getProducts().get(0);
            if (firstProduct.getKinguinId() == -1) {
                transactionType = TransactionType.BALANCE_PURCHASE;
            }
        }

        // Crear la transacción sin PIN y con estado COMPLETED
        Transaction transaction = new Transaction();
        transaction.setAmountUsd(orderAmount);
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        transaction.setExchangeRate(exchangeRate);
        transaction.setAmountHnl(currencyService.convertUsdToHnl(orderAmount, exchangeRate));

        // Asignar usuario o invitado
        if (orderRequest.getUserId() != null) {
            User user = userRepository.findById(orderRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
            transaction.setUser(user);
        } else if (orderRequest.getGuestId() != null && !orderRequest.getGuestId().isEmpty()) {
            transaction.setGuestId(orderRequest.getGuestId());
        } else {
            throw new RuntimeException("Debe proporcionar userId o guestId.");
        }

        transaction.setGameUserId(orderRequest.getGameUserId());
        transaction.setManual(orderRequest.getManual() != null ? orderRequest.getManual() : false);
        transaction.setExpiresAt(orderRequest.getManual() != null && orderRequest.getManual() ?
                LocalDateTime.now().plusMinutes(600) :
                LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        transaction.setType(transactionType);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription("Description");
        transaction.setPhoneNumber(phoneNumber);
        transaction.setOrderRequestNumber(orderRequest.getOrderRequestId());
        transaction.setTempPin(null);
        transaction.setTempPin(null);
        transaction.setStatus(TransactionStatus.COMPLETED);

        if (orderRequest.getProducts() != null && orderRequest.getProducts().size() > 10) {
            throw new IllegalArgumentException("No se pueden agregar más de 10 productos por transacción.");
        }

        List<TransactionProduct> transactionProducts = orderRequest.getProducts().stream().map(productRequest -> {
            Long productId = (long) productRequest.getKinguinId();
            TransactionProduct transactionProduct = new TransactionProduct();
            transactionProduct.setTransaction(transaction);
            transactionProduct.setProductId(productId);
            transactionProduct.setQuantity(productRequest.getQty());
            return transactionProduct;
        }).collect(Collectors.toList());
        transaction.setProducts(transactionProducts);

        // Guardar la transacción
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Actualizar estado
        updateTransactionStatus(savedTransaction.getId(), TransactionStatus.COMPLETED, "Transacción completada automáticamente mediante verificación de pago.");

        // Eliminar el pago no coincidente
        unmatchedPaymentRepository.delete(unmatchedPayment);

        // Notificar vía WebSocket
        webSocketController.notifyTransactionStatus(phoneNumber, "COMPLETED", "Su transacción fue completada exitosamente.", savedTransaction.getTransactionNumber());

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

            SmsMessage smsMessage = smsMessageRepository.findByReferenceNumber(refNumber);
            if (smsMessage == null) {
                throw new RuntimeException("No se encontró un SMS con el número de referencia proporcionado.");
            }

            matchingTransaction.setSmsMessage(smsMessage);
            smsMessage.setTransaction(matchingTransaction);
            transactionRepository.save(matchingTransaction);
            smsMessageRepository.save(smsMessage);

            if (transactionInDB.getManual()) {
                updateTransactionStatus(transactionInDB.getId(), TransactionStatus.AWAITING_MANUAL_PROCESSING, "El pago esta hecho. Su transaccion debe ser procesada.");

                transactionStorageService.addManualTransaction(transactionInDB);
                webSocketController.notifyTransactionStatus(phoneNumber, TransactionStatus.AWAITING_MANUAL_PROCESSING.name(), "Pendiente de procesamiento manual.", transactionInDB.getTransactionNumber());

                if (Boolean.TRUE.equals(transactionInDB.getManual())) {
                    manualVerificationWebSocketController.sendManualVerificationTransaction(transactionInDB);
                }

                System.out.println("Transacción manual agregada a la lista de espera. Transaction ID: " + transactionInDB.getTransactionNumber());
            } else {
                processTransaction(transactionInDB, amountReceived);
                cleanUpAfterSuccess(phoneNumber, matchingTransaction);
            }

        } catch (Exception e) {
            handleVerificationError(phoneNumber, e.getMessage());
        }
    }


    private Transaction getMatchingTransaction(String phoneNumber, Long pin) {
        List<Transaction> pendingTransactions = transactionStorageService.getPendingTransactions(phoneNumber);

        if (pendingTransactions.isEmpty()) {
            throw new RuntimeException("No hay transacciones pendientes para este número de teléfono.");
        }
        Optional<Transaction> optionalTransaction = pendingTransactions.stream()
                .filter(tx -> tx.getTempPin() != null && tx.getTempPin().equals(pin))
                .findFirst();

        if (!optionalTransaction.isPresent()) {
            throw new RuntimeException("No se encontró una transacción pendiente con el PIN proporcionado.");
        }

        return optionalTransaction.get();
    }

    private void processTransaction(Transaction transactionInDB, double amountReceivedHNL) {

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


    public List<Transaction> findPendingManualTransactions(){
        return transactionRepository.findByStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
    }

    public List<ManualVerificationWebSocketController.ManualVerificationTransactionDto> fetchPendingForApprovalTransaction(){
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
        return transactions.stream()
                .map( transaction -> new ManualVerificationWebSocketController.ManualVerificationTransactionDto(
                        transaction.getTransactionNumber(),
                        transaction.getAmountUsd(),
                        transaction.getAmountHnl(),
                        transaction.getExchangeRate(),
                        transaction.getTimestamp(),
                        transaction.getPhoneNumber(),
                        transaction.getUser() != null ? transaction.getUser().getEmail() : transaction.getGuestId(),
                        transaction.getProducts()
                ))
                .collect(Collectors.toList());
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
            updateTransactionStatus(transaction.getId(), TransactionStatus.EXPIRED, null);
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

        processTransaction(transaction, transaction.getAmountHnl());

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

        updateTransactionStatus(transaction.getId(), TransactionStatus.FAILED, "Transacción rechazada manualmente.");
    }
}
