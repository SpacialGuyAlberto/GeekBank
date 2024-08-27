package com.geekbank.bank.controllers;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.OrderResponse;
import com.geekbank.bank.services.TigoService;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.geekbank.bank.services.OrderService;
import com.geekbank.bank.services.SmsService;
import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.services.OrderRequestStorageService;
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
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsService smsService;


    @PostMapping
    public void placeOrder(@RequestBody OrderRequest orderRequest) {
        orderRequestStorageService.storeOrderRequest(orderRequest); // Almacena la orden en espera
    }
}
