package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.models.UnmatchedPayment;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.repositories.UnmatchedPaymentRepository;
import com.geekbank.bank.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    @Autowired
    private UnmatchedPaymentRepository unmatchedPaymentRepository;
    @Autowired
    public AccountController(AccountRepository accountRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Account> getAllAccounts(Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable("id") Long id) {
        Optional<Account> account = accountService.getAccountById(id);
        return account.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account createdAccount = accountService.createAccount(account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id) {
        accountService.deleteAccount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

//    @PostMapping("/apply-balance/{id}")
//    public ResponseEntity<Account> applyBalance(@PathVariable("id") Long id,
//                                                @RequestParam Double amount,
//                                                @RequestParam String paymentRefNumber) {
//        Optional<Account> optionalAccount = accountService.getAccountsByUserId(id);
//        Account account = optionalAccount.get();
//
//        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(paymentRefNumber);
//        if (!unmatchedPayment.isDifferenceRedeemed()){
//            unmatchedPayment.setDifferenceRedeemed(true);
//            unmatchedPaymentRepository.save(unmatchedPayment);
//            if (!optionalAccount.isPresent()) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//
//
//            account.setBalance(account.getBalance() + amount);
//            Account updatedAccount = accountRepository.save(account);
//            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
//
//        } else {
//            return new ResponseEntity<>(account, HttpStatus.OK);
//        }
//    }



    @PostMapping("/apply-balance/{id}")
    public ResponseEntity<?> applyBalance(
            @PathVariable("id") Long id,
            @RequestParam Double amount,
            @RequestParam String paymentRefNumber) {

        Optional<Account> optionalAccount = accountService.getAccountsByUserId(id);

        if (!optionalAccount.isPresent()) {
            return new ResponseEntity<>("Cuenta no encontrada.", HttpStatus.NOT_FOUND);
        }

        Account account = optionalAccount.get();
        UnmatchedPayment unmatchedPayment = unmatchedPaymentRepository.findByReferenceNumber(paymentRefNumber);

        if (unmatchedPayment == null) {
            return new ResponseEntity<>("No se encontró un pago con el número de referencia proporcionado.", HttpStatus.BAD_REQUEST);
        }

        if (!unmatchedPayment.isDifferenceRedeemed()) {
            unmatchedPayment.setDifferenceRedeemed(true);
            unmatchedPaymentRepository.save(unmatchedPayment);

            account.setBalance(account.getBalance() + amount);
            Account updatedAccount = accountRepository.save(account);

            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    "Un pago con ese número de referencia ya ha sido redimido. Por favor, ingrese otro número.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

}
