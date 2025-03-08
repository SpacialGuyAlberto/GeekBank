package com.geekbank.bank.repositories;

import com.geekbank.bank.models.SmsMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsMessageRepository extends JpaRepository<SmsMessage, Long> {

    List<SmsMessage> findBySenderPhoneNumber(String senderPhoneNumber);

    SmsMessage findByReferenceNumber(String referenceNumber);

    List<SmsMessage> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    SmsMessage findBySenderPhoneNumberAndReferenceNumber(String senderPhoneNumber, String referenceNumber);

    List<SmsMessage> findByTransactionIsNull();

    List<SmsMessage> findBySenderPhoneNumberAndAmountReceived(String senderPhoneNumber, double amountReceived);

    @Query("SELECT s FROM SmsMessage s WHERE s.senderPhoneNumber = ?1 AND s.amountReceived >= ?2")
    List<SmsMessage> findMessagesByPhoneNumberAndMinAmount(String senderPhoneNumber, double minAmount);

    Optional<SmsMessage> findByTransactionId(Long transactionId);
}
