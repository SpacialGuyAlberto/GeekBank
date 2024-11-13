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

    // Buscar por número de teléfono del remitente
    List<SmsMessage> findBySenderPhoneNumber(String senderPhoneNumber);

    // Buscar por número de referencia
    SmsMessage findByReferenceNumber(String referenceNumber);

    // Buscar mensajes dentro de un rango de fechas
    List<SmsMessage> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Buscar mensajes por teléfono y número de referencia (para coincidencias exactas)
    SmsMessage findBySenderPhoneNumberAndReferenceNumber(String senderPhoneNumber, String referenceNumber);

    // Buscar todos los mensajes no asociados a una transacción
    List<SmsMessage> findByTransactionIsNull();

    // Buscar mensajes por teléfono y monto recibido
    List<SmsMessage> findBySenderPhoneNumberAndAmountReceived(String senderPhoneNumber, double amountReceived);

    // Query personalizada para mensajes de un remitente y con un monto mínimo
    @Query("SELECT s FROM SmsMessage s WHERE s.senderPhoneNumber = ?1 AND s.amountReceived >= ?2")
    List<SmsMessage> findMessagesByPhoneNumberAndMinAmount(String senderPhoneNumber, double minAmount);

    Optional<SmsMessage> findByTransactionId(Long transactionId);
}
