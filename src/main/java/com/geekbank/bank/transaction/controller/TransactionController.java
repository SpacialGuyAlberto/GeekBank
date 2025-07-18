package com.geekbank.bank.transaction.controller;

import com.geekbank.bank.order.manual.controller.ManualVerificationWebSocketController;
import com.geekbank.bank.payment.tigo.dto.UnmatchedPaymentResponseDto;
import com.geekbank.bank.order.dto.OrderRequest;
import com.geekbank.bank.payment.tigo.model.UnmatchedPayment;
import com.geekbank.bank.transaction.repository.TransactionRepository;
import com.geekbank.bank.payment.tigo.repository.UnmatchedPaymentRepository;
import com.geekbank.bank.order.service.OrderRequestStorageService;
import com.geekbank.bank.transaction.service.TransactionService;
import com.geekbank.bank.transaction.service.TransactionStorageService;
import com.geekbank.bank.transaction.constants.TransactionStatus;
import com.geekbank.bank.transaction.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;

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


    @GetMapping("/verify-unmatched-payment")
    public ResponseEntity<UnmatchedPaymentResponseDto> verifyUnmatchedPaymentAmount(
            @RequestParam String referenceNumber,
            @RequestParam double expectedAmount) {

        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(referenceNumber);

        if (unmatchedPayment == null) {
            UnmatchedPaymentResponseDto responseDto = new UnmatchedPaymentResponseDto();
            responseDto.setMessage("El pago no fue encontrado con los datos proporcionados.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
        }

        double receivedAmount = unmatchedPayment.getAmountReceived();
        double difference = receivedAmount - expectedAmount;
        String message;
        List<String> options;

        if (difference == 0) {
            message = "El pago coincide con el monto esperado.";
            options = null;
            unmatchedPayment.setVerified(true);
            unmatchedPaymentRepository.save(unmatchedPayment);
        } else if (difference > 0) {
            message = "Hay una diferencia en el monto del pago.";
            unmatchedPayment.setVerified(true);
            unmatchedPaymentRepository.save(unmatchedPayment);
            options = Arrays.asList(
                    "Apply the difference as a balance",
                    difference > 1 ? "Return the difference" : "No se puede devolver la diferencia (debe ser mayor a 1)",
                    "Pay Anyways"
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

    @PostMapping("/create-transaction-promo-code")
    public void createTransactionWithAffiliate(
            @RequestBody OrderRequest orderRequest
    ){
        transactionService.createTransactionWithAffiliate(orderRequest);
    }
}
