package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import com.geekbank.bank.models.TransactionVerificationRequest;
import com.geekbank.bank.repositories.TransactionRepository;
import com.geekbank.bank.services.OrderRequestStorageService;
import com.geekbank.bank.services.TransactionService;
import com.geekbank.bank.services.TransactionStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionStorageService transactionStorageService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @GetMapping("/pending")
    public ResponseEntity<List<Transaction>> getPendingTransactionsByPhoneNumber(@RequestParam String phoneNumber) {
        List<Transaction> transactions = transactionService.findPendingTransactionsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping()
    public List<Transaction> getTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{userId}")
    public List<Transaction> getTransactionsById(@PathVariable long userId){
        return transactionService.getTransactionByUserId(userId);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> getTransactionsByUserIdAndTimestamp(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Transaction> transactions = transactionService.getTransactionsByUserIdAndTimestamp(userId, start, end);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/cancel/{transactionId}/{orderRequestId}")
    public ResponseEntity<Transaction> cancelRunningTransaction(
            @PathVariable String transactionId,
            @PathVariable String orderRequestId
    ) {
        Transaction transaction = transactionService.findByTransactionNumber(transactionId);
        transaction.setTempPin(0L);
        transactionStorageService.removeTransactionById(transaction.getId());
        transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.CANCELLED, "User Canceled");
        transactionRepository.save(transaction);
        orderRequestStorageService.removeOrderRequestById(orderRequestId);

        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/by-number/{transactionNumber}")
    public ResponseEntity<Transaction> getTransactionByNumber(@PathVariable String transactionNumber) {
        Transaction transaction = transactionService.findByTransactionNumber(transactionNumber);

        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/manual-pending")
    public ResponseEntity<List<Transaction>> getAwaitingTransaction(){
        List<Transaction> transactions = transactionService.findPendingManualTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/awaiting-approval")
    public ResponseEntity<List<ManualVerificationWebSocketController.ManualVerificationTransactionDto>> fetchAwaitingTransactions(){
        List<ManualVerificationWebSocketController.ManualVerificationTransactionDto> transactionDtOS = transactionService.fetchPendingForApprovalTransaction();
        return ResponseEntity.ok(transactionDtOS);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyTransaction(@RequestBody Map<String, String> verificationData) {
        String phoneNumber = verificationData.get("phoneNumber");
        Long pin = Long.valueOf(verificationData.get("pin"));
        String refNumber = verificationData.get("refNumber");

        try {
            transactionService.verifyTransaction(phoneNumber, pin, refNumber);
            return ResponseEntity.ok("Transacción verificada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al verificar la transacción: " + e.getMessage());
        }
    }

    @PostMapping("/aprove/{transactionNumber}")
    public ResponseEntity<?> approveTransaction(@PathVariable String transactionNumber) {
        try {
            transactionService.approveManualTransaction(transactionNumber);
            return ResponseEntity.ok("Transacción aprobada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/reject/{transactionNumber}")
    public ResponseEntity<?> rejectTransaction(@PathVariable String transactionNumber) {
        try {
            transactionService.rejectManualTransaction(transactionNumber);
            return ResponseEntity.ok("Transacción rechazada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
