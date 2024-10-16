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

        // Almacenar la solicitud de orden
        orderRequestStorageService.storeOrderRequest(orderRequest);

        // Crear la transacción con la lista de productos
        try {
            transactionService.createTransaction(
                    user,
                    orderRequest.getAmount(),
                    TransactionType.PURCHASE,
                    "Descripción",
                    orderRequest.getPhoneNumber(),
                    orderRequest.getProducts()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error al crear la transacción: " + e.getMessage());
        }

        return ResponseEntity.ok("Order placed successfully");
    }
}
