package com.geekbank.bank.transaction.repository;

import com.geekbank.bank.transaction.model.Transaction;
import com.geekbank.bank.transaction.constants.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long user_Id);
    List<Transaction> findAll();

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByTimestampBetweenAndUserId(LocalDateTime start, LocalDateTime end, Long userId);

    Transaction findByPhoneNumber(String phoneNUmber);
    List<Transaction> findByStatusAndPhoneNumber(TransactionStatus status, String phoneNumber);

    Transaction findByTransactionNumber(String transactionNumber);


    @Query("SELECT t FROM Transaction t WHERE t.transactionNumber = :transactionNumber")
    Transaction findTransactionByNumber(@Param("transactionNumber") String transactionNumber);

    List<Transaction> findByStatusAndTimestampBefore(TransactionStatus status, LocalDateTime timestamp);
    List<Transaction> findByGameUserId(Long gameUserId);
    List<Transaction> findByIsManual(Boolean isManual);
    Optional<Transaction> findByTempPin(Long tempPin);

}
