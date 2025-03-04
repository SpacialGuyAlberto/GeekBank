package com.geekbank.bank.services;

import com.geekbank.bank.controllers.ManualVerificationWebSocketController;
import com.geekbank.bank.controllers.TransactionWebSocketController;
import com.geekbank.bank.controllers.WebSocketController;
import com.geekbank.bank.exceptions.InsufficientBalanceException;
import com.geekbank.bank.exceptions.ResourceNotFoundException;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    OrderService orderService;

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private SmsMessageRepository smsMessageRepository;

    @Autowired
    private final SendGridEmailService emailService;

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
    private CurrencyService currencyService;

    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;

    @Autowired
    private ManualVerificationWebSocketController manualVerificationWebSocketController;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private SalesMetricsService salesMetricsService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    private static final long EXPIRATION_MINUTES = 3;

    private PriorityBlockingQueue<Transaction> manualVerificationQueue = new PriorityBlockingQueue<>(
            100,
            Comparator.comparing(Transaction::getTimestamp)
    );

    public TransactionService(SendGridEmailService emailService) {
        this.emailService = emailService;
    }
    private static Long generatePin() {
        Random random = new Random();
        int pin = random.nextInt(9000) + 1000; // entre 1000 y 9999
        return (long) pin;
    }

    @Transactional
    public Transaction createTransactionWithAffiliate(
            OrderRequest orderRequest
    ) {
        double baseAmountUsd = 0.0;
        for (OrderRequest.Product product : orderRequest.getProducts()) {
            baseAmountUsd += (product.getPrice() * product.getQty());
        }

        String affiliateLink = orderRequest.getAffiliateLink();
        String promoCode = orderRequest.getPromoCode();
        User affiliateUser = null;
        Double discount = 0.0;
        Double commission = 0.0;

        if (affiliateLink != null && !affiliateLink.isEmpty()) {
            affiliateUser = userRepository.findByAffiliateLink(affiliateLink);
        } else if (promoCode != null && !promoCode.isEmpty()) {
            affiliateUser = userRepository.findByPromoCode(promoCode);

            if (affiliateUser != null) {
                discount = baseAmountUsd * 0.05;
            }
        }
        double finalAmountUsd = baseAmountUsd - discount;
        if (finalAmountUsd < 0) finalAmountUsd = 0;

        if (affiliateUser != null && affiliateUser.getCommissionRate() != null) {
            double rate = affiliateUser.getCommissionRate();
            commission = finalAmountUsd * rate;
        }

        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        double amountHnl = currencyService.convertUsdToHnl(finalAmountUsd, exchangeRate);

        Transaction transaction = new Transaction();
        transaction.setAmountUsd(finalAmountUsd);
        transaction.setAmountHnl(amountHnl);
        transaction.setExchangeRate(exchangeRate);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setPhoneNumber(orderRequest.getPhoneNumber());
        transaction.setOrderRequestNumber(orderRequest.getOrderRequestId());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        transaction.setManual(false); // Ejemplo
        transaction.setAffiliate(affiliateUser);
        transaction.setDiscountApplied(discount);
        transaction.setCommissionEarned(commission);

        if (orderRequest.getUserId() != null) {
            User buyer = userRepository.findById(orderRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            transaction.setUser(buyer);
        } else if (orderRequest.getGuestId() != null) {
            transaction.setGuestId(orderRequest.getGuestId());
        }

        List<TransactionProduct> transactionProducts = new ArrayList<>();
        for (OrderRequest.Product product : orderRequest.getProducts()) {
            TransactionProduct tProd = new TransactionProduct();
            tProd.setTransaction(transaction);
            tProd.setProductId((long) product.getKinguinId());
            tProd.setQuantity(product.getQty());
            transactionProducts.add(tProd);
        }
        transaction.setProducts(transactionProducts);
        Transaction saved = transactionRepository.save(transaction);
        // 10. (Opcional) Notificar o seguir tu flujo normal (por ejemplo, placeOrder)
        // orderService.placeOrder(orderRequest, saved);
        return saved;
    }
    /**
     * Crea una transacción “genérica” con datos básicos (pendiente de pago).
     */
    @Transactional
    public Transaction createTransaction(
            User user,
            String guestId,
            Long gameUserId,
            String orderRequestNumber,
            double amountUSD,
            TransactionType type,
            String description,
            String phoneNumber,
            List<OrderRequest.Product> products,
            Boolean isManual
    ) {
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        double amountHNL = currencyService.convertUsdToHnl(amountUSD, exchangeRate);

        // Construye la transacción
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

        if (gameUserId != null) {
            transaction.setGameUserId(gameUserId);
        }

        if (isManual != null) {
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
        // Inicia en estado PENDING (si aún no se ha pagado)
        transaction.setStatus(TransactionStatus.PENDING);

        // Validar cantidad máxima de productos
        if (products != null && products.size() > 10) {
            throw new IllegalArgumentException("No se pueden agregar más de 10 productos por transacción.");
        }
        // Asocia productos
        List<TransactionProduct> transactionProducts = products.stream().map(productRequest -> {
            Long productId = (long) productRequest.getKinguinId();
            TransactionProduct transactionProduct = new TransactionProduct();
            transactionProduct.setTransaction(transaction);
            transactionProduct.setProductId(productId);
            transactionProduct.setQuantity(productRequest.getQty());
            return transactionProduct;
        }).collect(Collectors.toList());
        transaction.setProducts(transactionProducts);

        // Guarda en DB
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Lo agrega a “pending transactions” en tu almacenamiento temporal
        transactionStorageService.storePendingTransaction(savedTransaction);

        // Notifica (opcional)
        updateTransactionStatus(savedTransaction.getId(), TransactionStatus.PENDING, null);
        return savedTransaction;
    }

    /**
     * Compra con balance interno.
     * Si NO hace llamada a Kinguin, se puede completar inmediatamente.
     * Si sí hace llamada a Kinguin, ponerla en PROCESSING y luego completarla en KeyPollingService.
     */
    @Transactional
    public Transaction purchaseWithBalance(
            Long userId,
            String orderRequestNumber,
            List<OrderRequest.Product> products,
            String phoneNumber,
            OrderRequest orderRequest
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        double totalAmountUsd = products.stream()
                .mapToDouble(product -> product.getPrice() * product.getQty())
                .sum();

        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        double totalAmountHnl = currencyService.convertUsdToHnl(totalAmountUsd, exchangeRate);

        // 4. Cuenta del usuario
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada para el usuario con ID: " + userId));

        // 5. Verificar saldo
        if (account.getBalance() < totalAmountUsd) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar la compra.");
        }

        // 6. Deducir saldo
        account.setBalance(account.getBalance() - totalAmountUsd);
        accountRepository.save(account);

        // 7. Crear transacción
        Transaction transaction = new Transaction();
        transaction.setAmountUsd(totalAmountUsd);
        transaction.setAmountHnl(totalAmountHnl);
        transaction.setExchangeRate(exchangeRate);
        transaction.setUser(user);
        transaction.setManual(false);  // asumiendo no es manual
        transaction.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        transaction.setType(TransactionType.BALANCE_PURCHASE);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription("Compra con balance");
        transaction.setPhoneNumber(phoneNumber);
        transaction.setOrderRequestNumber(orderRequestNumber);
        // **Importante**: Si vas a llamar a Kinguin, pon PROCESSING en lugar de COMPLETED
        // transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTempPin(null);

        // 8. Validar productos
        if (products != null && products.size() > 10) {
            throw new IllegalArgumentException("No se pueden agregar más de 10 productos por transacción.");
        }
        // 9. Asociar productos
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

        // Si no necesitas keys, la transacción puede quedar COMPLETED de inmediato
        // (aquí se asume que no invocas a Kinguin).
        salesMetricsService.onTransactionCompleted(savedTransaction);

        // Si SÍ necesitas keys, haz:
        //    transaction.setStatus(TransactionStatus.PROCESSING);
        //    transactionRepository.save(savedTransaction);
        //    orderService.placeOrder(orderRequest, savedTransaction);

        // Notificamos al front
        webSocketController.notifyTransactionStatus(
                phoneNumber,
                TransactionStatus.COMPLETED.name(),
                "Compra realizada exitosamente utilizando tu balance.",
                savedTransaction.getTransactionNumber()
        );

        return savedTransaction;
    }

    /**
     * Transacción creada cuando se verifica un pago Tigo (unmatchedPayment verificado).
     * Si la transacción NO es manual, la ponemos en PROCESSING y hacemos placeOrder.
     * Cuando KeyPollingService entregue las keys y correo, se marca COMPLETED.
     */
    @Transactional
    public Transaction createTransactionForVerifiedTigoPayment(OrderRequest orderRequest) {
        // 1. Busca el unmatchedPayment
        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(orderRequest.getRefNumber());
        if (!unmatchedPayment.isVerified()) {
            throw new RuntimeException("El pago encontrado no ha sido verificado.");
        }

        double orderAmount = orderRequest.getAmount();
        double amountReceived = unmatchedPayment.getAmountReceived();

        TransactionType transactionType = TransactionType.PURCHASE;
        if (orderRequest.getProducts() != null && !orderRequest.getProducts().isEmpty()) {
            OrderRequest.Product firstProduct = orderRequest.getProducts().get(0);
            if (firstProduct.getKinguinId() == -1) {
                transactionType = TransactionType.BALANCE_PURCHASE;
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAmountUsd(orderAmount);
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        transaction.setExchangeRate(exchangeRate);
        transaction.setAmountHnl(orderAmount);

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
        transaction.setExpiresAt(transaction.getManual()
                ? LocalDateTime.now().plusMinutes(600)
                : LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        transaction.setType(transactionType);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription("Description");
        transaction.setPhoneNumber(orderRequest.getPhoneNumber());
        transaction.setOrderRequestNumber(orderRequest.getOrderRequestId());
        transaction.setTempPin(null);

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

        // Estado inicial: si es manual => AWAITING_MANUAL_PROCESSING
        // si no => PROCESSING (porque vamos a descargar keys de Kinguin)
        if (transaction.getManual()) {
            transaction.setStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
        } else {
            transaction.setStatus(TransactionStatus.PROCESSING);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Si es manual => no llamamos a placeOrder.
        // Si no, placeOrder => KeyPollingService marcará COMPLETED al entregar keys.
        if (!transaction.getManual()) {
            OrderResponse orderResponse = orderService.placeOrder(orderRequest, savedTransaction);
            System.out.println(orderResponse);
        } else {
            processManualTransaction(savedTransaction);
            this.emailService.sendNotificationEmail("enkiluzlbel@gmail.com");
        }
        
        webSocketController.sendTransactionStatus(savedTransaction.getStatus());
        return savedTransaction;
    }

    /**
     * Creación de transacción para Paypal / Tarjeta.
     * Mismo patrón: si no es manual => PROCESSING y se invoca placeOrder.
     * KeyPollingService completará la transacción.
     */
    @Transactional
    public Transaction createTransactionForPaypalAndCreditCard(OrderRequest orderRequest) {
        double orderAmount = orderRequest.getProducts().stream()
                .mapToDouble(p -> {
                    double finalPrice = pricingService.calculateSellingPrice(p.getPrice());
                    return finalPrice * p.getQty();
                })
                .sum();

        TransactionType transactionType = TransactionType.PURCHASE;
        if (orderRequest.getProducts() != null && !orderRequest.getProducts().isEmpty()) {
            OrderRequest.Product firstProduct = orderRequest.getProducts().get(0);
            if (firstProduct.getKinguinId() == -1) {
                transactionType = TransactionType.BALANCE_PURCHASE;
            }
        }

        Transaction transaction = new Transaction();
        transaction.setAmountUsd(orderAmount);
        double exchangeRate = currencyService.getExchangeRateUSDtoHNL();
        transaction.setExchangeRate(exchangeRate);
        transaction.setAmountHnl(currencyService.convertUsdToHnl(orderAmount, exchangeRate));

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
        transaction.setExpiresAt(transaction.getManual()
                ? LocalDateTime.now().plusMinutes(600)
                : LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        transaction.setType(transactionType);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setDescription("Description");
        transaction.setPhoneNumber(orderRequest.getPhoneNumber());
        transaction.setOrderRequestNumber(orderRequest.getOrderRequestId());
        transaction.setTempPin(null);

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

        // Estado inicial
        if (transaction.getManual()) {
            transaction.setStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
        } else {
            transaction.setStatus(TransactionStatus.PROCESSING);
            UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(orderRequest.getRefNumber());
            if (!unmatchedPayment.isConsumed()){
                unmatchedPayment.setConsumed(true);
                unmatchedPaymentRepository.save(unmatchedPayment);
            }
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        // placeOrder solo si no es manual
        if (!transaction.getManual()) {
            OrderResponse orderResponse = orderService.placeOrder(orderRequest, savedTransaction);
            System.out.println(orderResponse);
        } else {
            processManualTransaction(savedTransaction);
            this.emailService.sendNotificationEmail("enkiluzlbel@gmail.com");
        }

        webSocketController.sendTransactionStatus(savedTransaction.getStatus());
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

            Transaction transactionInDB = transactionRepository.findByTransactionNumber(matchingTransaction.getTransactionNumber());
            if (transactionInDB == null) {
                throw new RuntimeException("Transacción no encontrada en la base de datos.");
            }

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

            // Si es manual => AWAITING_MANUAL_PROCESSING
            // Sino => procesamos de una vez (lo que hace processTransaction)
            if (transactionInDB.getManual()) {
                updateTransactionStatus(transactionInDB.getId(), TransactionStatus.AWAITING_MANUAL_PROCESSING,
                        "El pago está hecho. Su transacción debe ser procesada manualmente.");
                transactionStorageService.addManualTransaction(transactionInDB);
                webSocketController.notifyTransactionStatus(
                        phoneNumber,
                        TransactionStatus.AWAITING_MANUAL_PROCESSING.name(),
                        "Pendiente de procesamiento manual.",
                        transactionInDB.getTransactionNumber()
                );

                if (Boolean.TRUE.equals(transactionInDB.getManual())) {
                    manualVerificationWebSocketController.sendManualVerificationTransaction(transactionInDB);
                }

                System.out.println("Transacción manual agregada a la lista de espera. Transaction ID: " + transactionInDB.getTransactionNumber());
            } else {
                // No manual => la procesamos
                processTransaction(transactionInDB, amountReceived);
                cleanUpAfterSuccess(phoneNumber, matchingTransaction);
            }

        } catch (Exception e) {
            handleVerificationError(phoneNumber, e.getMessage());
        }
    }

    /**
     * Obtiene la transacción pendiente (con un PIN) que coincida con el teléfono y pin.
     */
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

    /**
     * Procesa la transacción (si ya está pagada) dependiendo del tipo (BALANCE_PURCHASE o PURCHASE).
     */
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

    /**
     * Añade saldo a la cuenta y marca la transacción como COMPLETED.
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
        salesMetricsService.onTransactionCompleted(transactionInDB);
        System.out.println("Balance actualizado para el usuario: " + user.getEmail());
    }

    /**
     * Marca la transacción como COMPLETED en la base de datos (COMPRA DE PRODUCTO).
     * En la versión con KeyPollingService, la transacción pasa a COMPLETED solo cuando
     * ya se enviaron las keys y el email. (Esto lo hace el KeyPollingService, no aquí).
     *
     * Si no requieres Kinguin, puedes hacerlo de inmediato acá.
     */
    private void processProductPurchase(Transaction transactionInDB) {
        updateTransactionStatus(transactionInDB.getId(), TransactionStatus.COMPLETED, null);
        salesMetricsService.onTransactionCompleted(transactionInDB);
        System.out.println("Transacción de compra de producto completada. Transaction ID: " + transactionInDB.getTransactionNumber());
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

    /**
     * Limpia el almacenamiento temporal y notifica COMPLETED al front.
     */
    private void cleanUpAfterSuccess(String phoneNumber, Transaction matchingTransaction) {
        transactionStorageService.removeTransaction(phoneNumber, matchingTransaction.getTransactionNumber());
        transactionStorageService.removeSmsReferenceNumber(phoneNumber);
        transactionStorageService.removeAmountReceived(phoneNumber);
        orderRequestStorageService.removeOrderRequest(phoneNumber);

        webSocketController.notifyTransactionStatus(
                phoneNumber,
                "COMPLETED",
                "Your transaction was successfully completed.",
                matchingTransaction.getTransactionNumber()
        );
    }

    /**
     * Maneja errores de verificación.
     */
    private void handleVerificationError(String phoneNumber, String errorMessage) {
        System.err.println("Error al verificar la transacción: " + errorMessage);
        webSocketController.notifyTransactionStatus(phoneNumber, "FAILED", errorMessage, null);
    }

    /**
     * Actualiza el estado de la transacción y notifica por WebSocket.
     */
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

    /**
     * Control para ver si el estado actual permite actualización.
     */
    private boolean isUpdatableStatus(TransactionStatus currentStatus) {
        // Si está CANCELLED o EXPIRED, no se puede cambiar.
        return currentStatus != TransactionStatus.CANCELLED && currentStatus != TransactionStatus.EXPIRED;
    }

    /**
     * Procesa transacción manual: la agrega a una cola y notifica por WebSocket.
     */
    public void processManualTransaction(Transaction transaction) {
        manualVerificationQueue.offer(transaction);
        manualVerificationWebSocketController.sendManualVerificationTransaction(transaction);
    }

    // --------------------------------------------------------------
    //   BÚSQUEDAS Y OTROS MÉTODOS AUXILIARES
    // --------------------------------------------------------------

    public List<Transaction> findTransactionByGameUserId(Long gameUserId) {
        return transactionRepository.findByGameUserId(gameUserId);
    }

    public List<Transaction> findPendingTransactionsByPhoneNumber(String phoneNumber) {
        return transactionRepository.findByStatusAndPhoneNumber(TransactionStatus.PENDING, phoneNumber);
    }

    public List<Transaction> findPendingManualTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
    }

    public List<ManualVerificationWebSocketController.ManualVerificationTransactionDto> fetchPendingForApprovalTransaction() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.AWAITING_MANUAL_PROCESSING);
        return transactions.stream()
                .map(transaction -> new ManualVerificationWebSocketController.ManualVerificationTransactionDto(
                        transaction.getTransactionNumber(),
                        transaction.getAmountUsd(),
                        transaction.getAmountHnl(),
                        transaction.getExchangeRate(),
                        transaction.getTimestamp(),
                        transaction.getPhoneNumber(),
                        transaction.getUser() != null ? transaction.getUser().getEmail() : transaction.getGuestId(),
                        transaction.getProducts()
                ))
                .sorted(Comparator.comparing(ManualVerificationWebSocketController.ManualVerificationTransactionDto::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public Transaction findByTransactionNumber(String transactionNumber) {
        return transactionRepository.findByTransactionNumber(transactionNumber);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    private String generateTransactionNumber() {
        return "TX-" + System.currentTimeMillis();
    }

    public List<Transaction> getTransactionsByUserIdAndTimestamp(Long userId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetweenAndUserId(start, end, userId);
    }

    public List<Transaction> getTransactionsByTimestamp(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTimestampBetween(start, end);
    }

    /**
     * Expira las transacciones en estado PENDING que ya pasaron el tiempo límite.
     * Se ejecuta cada 360000 ms (6 minutos).
     */
    @Scheduled(fixedRate = 360000)
    @Transactional
    public void expireTransaction() {
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

    /**
     * Aprobación manual de transacciones.
     */
    @Transactional
    public void approveManualTransaction(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber);
        if (transaction == null) {
            throw new RuntimeException("Transacción no encontrada.");
        }
        if (transaction.getStatus() != TransactionStatus.AWAITING_MANUAL_PROCESSING) {
            throw new RuntimeException("La transacción no está en estado de verificación manual.");
        }

        // Asumimos que, al aprobar manualmente, la transacción ya está pagada
        // y solo quedaría completarla. Llamamos processTransaction con el amountHnl
        processTransaction(transaction, transaction.getAmountHnl());

        // Dejarla en COMPLETED
        updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED, "Transacción aprobada manualmente.");
        salesMetricsService.onTransactionCompleted(transaction);
    }

    /**
     * Rechazo manual de transacciones.
     */
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
