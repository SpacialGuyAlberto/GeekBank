package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

//    List<Transaction> findByUserId(Long userId);
    List<Transaction> findAll();

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Transaction findByPhoneNumber(String phoneNUmber);
    List<Transaction> findByStatusAndPhoneNumber(TransactionStatus status, String phoneNumber);

    Transaction findByTransactionNumber(String transactionNumber);
}
