package com.geekbank.bank.order.controller;

import com.geekbank.bank.common.exceptions.InsufficientBalanceException;
import com.geekbank.bank.common.exceptions.ResourceNotFoundException;
import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.order.model.Orders;
import com.geekbank.bank.order.service.OrderService;
import com.geekbank.bank.order.repository.OrdersRepository;
import com.geekbank.bank.payment.tigo.model.UnmatchedPayment;
import com.geekbank.bank.transaction.repository.TransactionRepository;
import com.geekbank.bank.payment.tigo.repository.UnmatchedPaymentRepository;
import com.geekbank.bank.transaction.dto.TransactionResponse;
import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.transaction.service.TransactionService;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

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
        unmatchedPaymentRepository.save(unmatchedPayment);
        //        if (!unmatchedPayment.isConsumed()){
//            unmatchedPayment.setConsumed(true);
//            unmatchedPaymentRepository.save(unmatchedPayment);
//        }

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
