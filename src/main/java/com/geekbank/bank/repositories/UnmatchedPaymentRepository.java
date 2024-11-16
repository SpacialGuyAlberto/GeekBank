package com.geekbank.bank.repositories;

import com.geekbank.bank.models.UnmatchedPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UnmatchedPaymentRepository extends JpaRepository<UnmatchedPayment, Long> {

    // Buscar pagos no coincidentes por número de teléfono
    List<UnmatchedPayment> findByPhoneNumber(String phoneNumber);

    // Buscar pagos no coincidentes por número de referencia
    List<UnmatchedPayment> findByReferenceNumber(String referenceNumber);

    // Buscar pagos no coincidentes dentro de un rango de fechas
    List<UnmatchedPayment> findByReceivedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Buscar un pago específico por número de teléfono y referencia
    UnmatchedPayment findByPhoneNumberAndReferenceNumber(String phoneNumber, String referenceNumber);

    // Buscar pagos no coincidentes por teléfono y monto recibido
    List<UnmatchedPayment> findByPhoneNumberAndAmountReceived(String phoneNumber, double amountReceived);

    // Buscar pagos que no tienen un mensaje SMS asociado
    List<UnmatchedPayment> findBySmsMessageIsNull();

    UnmatchedPayment findByReferenceNumberAndPhoneNumber(String referenceNumber, String phoneNumber);
}
