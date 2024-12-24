package com.geekbank.bank.controllers;

import com.geekbank.bank.exceptions.InsufficientBalanceException;
import com.geekbank.bank.exceptions.ResourceNotFoundException;
import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.OrdersRepository;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.repositories.UnmatchedPaymentRepository;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private TigoService tigoService;
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;
    Orders orders;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsService smsService;
    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;
    @Autowired
    private OrdersRepository ordersRepository;



    @PostMapping("/create-order-for-verified-tigo-payment")
    public ResponseEntity<Transaction> placeOrderAndTransactionForTigoVerifiedPayment(@RequestBody OrderRequest orderRequest){
        User user = null;

        if (orderRequest.getUserId() != null){
            user = userRepository.findById(orderRequest.getUserId())
                    .orElse(null);
        }

        Transaction transaction = transactionService.createTransactionForVerifiedTigoPayment(orderRequest);
        orderService.createOrder(orderRequest, transaction);

        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(orderRequest.getRefNumber());
        if (!unmatchedPayment.isConsumed()){
            unmatchedPayment.setConsumed(true);
            unmatchedPaymentRepository.save(unmatchedPayment);
        }

        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/create-order-for-paypal-and-credit-card")
    public ResponseEntity<Transaction> placeOrderAndTransactionForPaypalAndCreditCard(@RequestBody OrderRequest orderRequest){
        User user = null;

        if (orderRequest.getUserId() != null){
            user = userRepository.findById(orderRequest.getUserId())
                    .orElse(null);
        }

        Transaction transaction = transactionService.createTransactionForPaypalAndCreditCard(orderRequest);
        orderService.createOrder(orderRequest, transaction);
        return ResponseEntity.ok(transaction);
    }


//    @PostMapping
//    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
//        User user = null;
//
//        if (orderRequest.getUserId() != null){
//            user = userRepository.findById(orderRequest.getUserId())
//                    .orElse(null);
//        }
//        orderRequest.setOrderRequestId();
//        orderRequestStorageService.storeOrderRequest(orderRequest);
//
//        TransactionType transactionType = TransactionType.PURCHASE;
//
//        if (orderRequest.getProducts() != null && !orderRequest.getProducts().isEmpty()) {
//            OrderRequest.Product firstProduct = orderRequest.getProducts().get(0);
//            if ( firstProduct.getKinguinId() == -1) {
//                transactionType = TransactionType.BALANCE_PURCHASE;
//            }
//        }
//
//        Transaction savedTransaction;
//        Boolean isManual = (orderRequest.getManual() != null) ? orderRequest.getManual() : false;
//        try {
//            savedTransaction = transactionService.createTransaction(
//                    user,
//                    orderRequest.getGuestId(),
//                    orderRequest.getGameUserId(),
//                    orderRequest.getOrderRequestId(),
//                    orderRequest.getAmount(),
//                    transactionType,
//                    "Descripción",
//                    orderRequest.getPhoneNumber(),
//                    orderRequest.getProducts(),
//                    isManual
//            );
//        }  catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(500).body("Error al crear la transacción: " + e.getMessage());
//        }
//
//        TransactionResponse response = new TransactionResponse();
//        response.setOrderRequestNumber(orderRequest.getOrderRequestId());
//        response.setTransactionNumber(savedTransaction.getTransactionNumber());
//        response.setTempPin(savedTransaction.getTempPin());
//        response.setTransactionStatus(TransactionStatus.PENDING);
//        System.out.println("MANUAL TRANSACTION : " + orderRequest.getManual());
//
//        String transactionNumber = savedTransaction.getTransactionNumber();
//        Long tempPin = savedTransaction.getTempPin();
//        TransactionStatus transactionStatus = savedTransaction.getStatus();
//        String responseMessage = "Order placed successfully: " + orderRequest.getOrderRequestId() + "\n Transaction number: "  + transactionNumber + "\n PIN" + tempPin.toString() + "\n Status" + transactionStatus.name();
//
//
//        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(orderRequest.getRefNumber());
//        if (!unmatchedPayment.isConsumed()){
//            unmatchedPayment.setConsumed(true);
//            unmatchedPaymentRepository.save(unmatchedPayment);
//        }
//
//        return ResponseEntity.ok(responseMessage);
//    }

    @PostMapping("/purchase-with-balance")
    public ResponseEntity<?> purchaseWithBalance(@RequestBody OrderRequest orderRequest) {
        try {
            if (orderRequest.getUserId() == null) {
                return ResponseEntity.badRequest().body("El ID del usuario es requerido.");
            }

            if (orderRequest.getProducts() == null || orderRequest.getProducts().isEmpty()) {
                return ResponseEntity.badRequest().body("Debe haber al menos un producto en la solicitud.");
            }

            orderRequest.setOrderRequestId();

            Transaction transaction = transactionService.purchaseWithBalance(
                    orderRequest.getUserId(),
                    orderRequest.getOrderRequestId(),
                    orderRequest.getProducts(),
                    orderRequest.getPhoneNumber(),
                    orderRequest
            );

            TransactionResponse response = new TransactionResponse();
            response.setOrderRequestNumber(transaction.getOrderRequestNumber());
            response.setTransactionNumber(transaction.getTransactionNumber());
            response.setTransactionStatus(transaction.getStatus());

            return ResponseEntity.ok(response);


        } catch (InsufficientBalanceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Saldo insuficiente: " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la compra: " + e.getMessage());
        }
    }

    @GetMapping("/find-by-transaction/{transactionNumber}")
    public ResponseEntity<Orders> fetchOrder(@PathVariable("transactionNumber") String transactionNumber) {
        try {
            Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber);
            Orders orders = ordersRepository.findByTransaction_Id(transaction.getId());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
