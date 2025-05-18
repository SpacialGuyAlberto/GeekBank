package com.geekbank.bank.order.service;

import com.geekbank.bank.user.admin.controller.WebSocketController;
import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.order.dto.OrderResponse;
import com.geekbank.bank.order.model.Orders;
import com.geekbank.bank.order.repository.OrdersRepository;
import com.geekbank.bank.order.support.KeyPollingService;
import com.geekbank.bank.support.receipt.utils.PdfGeneratorService;
import com.geekbank.bank.support.email.service.SendGridEmailService;
import com.geekbank.bank.support.sms.service.SmsService;
import com.geekbank.bank.transaction.repository.TransactionRepository;
import com.geekbank.bank.transaction.constants.TransactionStatus;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private KeyPollingService keyPollingService;

    @Value("${KINGUIN_ORDER_URL}")
    private String KINGUIN_ORDER_URL;

    @Value("${KINGUIN_API_KEY}")
    private  String API_KEY;

    public Orders createOrder(OrderRequest orderRequest, Transaction transaction){
        Orders order = new Orders();
        order.setTransaction(transaction);
        order.setUserId(orderRequest.getUserId());
        order.setGameUserId(orderRequest.getGameUserId());
        order.setProducts(transaction.getProducts());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setGuestId(orderRequest.getGuestId());
        order.setRefNumber(orderRequest.getRefNumber());
        order.setAmount(orderRequest.getAmount());
        order.setManual(orderRequest.getManual());
        order.setCreatedAt(LocalDateTime.now());

        return ordersRepository.save(order);
    }

    public OrderResponse placeOrder(OrderRequest orderRequest, Transaction transaction) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", API_KEY);
        headers.set("Content-Type", "application/json");

        HttpEntity<OrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                KINGUIN_ORDER_URL,
                HttpMethod.POST,
                entity,
                OrderResponse.class
        );

        OrderResponse orderResponse = response.getBody();

        if (orderResponse != null && orderResponse.getOrderId() != null) {
            System.out.println("Order placed successfully with ID: " + orderResponse.getOrderId());

            transaction.setStatus(TransactionStatus.PROCESSING);
            webSocketController.sendTransactionStatus(transaction.getStatus());

            transaction.setExternalOrderId(orderResponse.getOrderId());
            transactionRepository.save(transaction);

            keyPollingService.pollKeysIndefinitely(orderResponse.getOrderId(), transaction, orderRequest);

            return orderResponse;
        } else {
            System.err.println("Order failed or did not return a valid Order ID.");
            throw new RuntimeException("Error placing order with Kinguin");
        }
    }

}
