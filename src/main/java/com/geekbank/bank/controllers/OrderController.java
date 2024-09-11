package com.geekbank.bank.controllers;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.OrderResponse;
import com.geekbank.bank.models.TransactionType;
import com.geekbank.bank.services.*;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.geekbank.bank.models.OrderRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private TigoService tigoService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsService smsService;


    @PostMapping
    public void placeOrder(@RequestBody OrderRequest orderRequest) {
        orderRequestStorageService.storeOrderRequest(orderRequest);
        ///1.-TRANSAKTION ERSTELLEN
//        transactionService.createTransaction()

                //    public Transaction createTransaction(Long userId, double amount, TransactionType type, String description) {
        ///la transaccion debe incluir el numero de telefono tambien
          transactionService.createTransaction(orderRequest.getUser(), orderRequest.getAmount(), TransactionType.PURCHASE, "No she", orderRequest.getPhoneNumber());
          //guardar en transactions storage service
            //transaction.storeTransaction()

    }
}
