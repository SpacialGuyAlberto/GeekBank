package com.geekbank.bank.controllers;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.UserRepository;
import com.geekbank.bank.services.*;
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
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsService smsService;


    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        User user = null;

        if (orderRequest.getUserId() != null){
            user = userRepository.findById(orderRequest.getUserId())
                    .orElse(null);
        }
        orderRequest.setOrderRequestId();
        orderRequestStorageService.storeOrderRequest(orderRequest);

        TransactionType transactionType = TransactionType.PURCHASE;

        if (orderRequest.getProducts() != null && !orderRequest.getProducts().isEmpty()) {
            OrderRequest.Product firstProduct = orderRequest.getProducts().get(0);
            if ( firstProduct.getKinguinId() == -1) {
                transactionType = TransactionType.BALANCE_PURCHASE;
            }
        }

        Transaction savedTransaction;

        try {
            savedTransaction = transactionService.createTransaction(
                    user,
                    orderRequest.getAmount(),
                    transactionType,
                    "Descripción",
                    orderRequest.getPhoneNumber(),
                    orderRequest.getProducts()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error al crear la transacción: " + e.getMessage());
        }
        String transactionNumber = savedTransaction.getTransactionNumber();
        String responseMessage = "Order placed successfully: " + orderRequest.getOrderRequestId() + "\n Transaction number: "  + transactionNumber;

        return ResponseEntity.ok(responseMessage);
    }
}
