package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import com.geekbank.bank.models.TransactionType;
import com.geekbank.bank.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;
    private User user;

    @BeforeEach
    public void setup() {
        transactionRepository.deleteAll();

        user = new User();
        user.setId(1L); // Simula un usuario ya existente

        // Crear y guardar transacciones de prueba
        transaction1 = new Transaction();
        transaction1.setTransactionNumber("TXN001");
        transaction1.setAmountUsd(100.00);
        transaction1.setPhoneNumber("1234567890");
        transaction1.setType(TransactionType.PURCHASE);
        transaction1.setTimestamp(LocalDateTime.now().minusDays(5));
        transaction1.setStatus(TransactionStatus.COMPLETED);
        transaction1.setUser(user);
        transaction1.setExpiresAt(LocalDateTime.now().plusDays(10));

        transaction2 = new Transaction();
        transaction2.setTransactionNumber("TXN002");
        transaction2.setAmountUsd(200.00);
        transaction2.setPhoneNumber("0987654321");
        transaction2.setType(TransactionType.REFUND);
        transaction2.setTimestamp(LocalDateTime.now().minusDays(3));
        transaction2.setStatus(TransactionStatus.PENDING);
        transaction2.setUser(user);
        transaction2.setExpiresAt(LocalDateTime.now().plusDays(15));

        transaction3 = new Transaction();
        transaction3.setTransactionNumber("TXN003");
        transaction3.setAmountUsd(150.00);
        transaction3.setPhoneNumber("1234567890");
        transaction3.setType(TransactionType.PURCHASE);
        transaction3.setTimestamp(LocalDateTime.now().minusDays(1));
        transaction3.setStatus(TransactionStatus.FAILED);
        transaction3.setExpiresAt(LocalDateTime.now().plusDays(5));

        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        transactionRepository.save(transaction3);
    }

    @Test
    @DisplayName("Buscar transacciones por ID de usuario")
    public void testFindByUserId() {
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        assertEquals(2, transactions.size(), "Debería encontrar dos transacciones para el ID de usuario proporcionado");
    }

    @Test
    @DisplayName("Buscar transacciones por estado")
    public void testFindByStatus() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.PENDING);
        assertEquals(1, transactions.size(), "Debería encontrar una transacción con estado PENDING");
        assertEquals("TXN002", transactions.get(0).getTransactionNumber(), "El número de transacción debería ser TXN002");
    }

    @Test
    @DisplayName("Buscar transacciones por rango de fechas")
    public void testFindByTimestampBetween() {
        LocalDateTime start = LocalDateTime.now().minusDays(4);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = transactionRepository.findByTimestampBetween(start, end);
        assertEquals(2, transactions.size(), "Debería encontrar dos transacciones en el rango de fechas especificado");
    }

    @Test
    @DisplayName("Buscar transacciones por rango de fechas e ID de usuario")
    public void testFindByTimestampBetweenAndUserId() {
        LocalDateTime start = LocalDateTime.now().minusDays(4);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> transactions = transactionRepository.findByTimestampBetweenAndUserId(start, end, user.getId());
        assertEquals(1, transactions.size(), "Debería encontrar una transacción para el usuario en el rango de fechas especificado");
        assertEquals("TXN002", transactions.get(0).getTransactionNumber(), "El número de transacción debería ser TXN002");
    }

    @Test
    @DisplayName("Buscar transacción por número de teléfono")
    public void testFindByPhoneNumber() {
        Transaction transaction = transactionRepository.findByPhoneNumber("1234567890");
        assertNotNull(transaction, "La transacción no debería ser nula");
        assertEquals("TXN001", transaction.getTransactionNumber(), "El número de transacción debería ser TXN001");
    }

    @Test
    @DisplayName("Buscar transacciones por estado y número de teléfono")
    public void testFindByStatusAndPhoneNumber() {
        List<Transaction> transactions = transactionRepository.findByStatusAndPhoneNumber(TransactionStatus.FAILED, "1234567890");
        assertEquals(1, transactions.size(), "Debería encontrar una transacción con estado FAILED y el número de teléfono especificado");
        assertEquals("TXN003", transactions.get(0).getTransactionNumber(), "El número de transacción debería ser TXN003");
    }

    @Test
    @DisplayName("Buscar transacción por número de transacción")
    public void testFindByTransactionNumber() {
        Transaction transaction = transactionRepository.findByTransactionNumber("TXN002");
        assertNotNull(transaction, "La transacción no debería ser nula");
        assertEquals(200.00, transaction.getAmountUsd(), "El monto de la transacción debería ser 200.00");
    }

    @Test
    @DisplayName("Buscar transacciones por estado y fecha de expiración antes de una fecha")
    public void testFindByStatusAndTimestampBefore() {
        LocalDateTime timestamp = LocalDateTime.now().minusDays(2);

        List<Transaction> transactions = transactionRepository.findByStatusAndTimestampBefore(TransactionStatus.COMPLETED, timestamp);
        assertEquals(1, transactions.size(), "Debería encontrar una transacción con estado COMPLETED y una fecha antes del límite especificado");
        assertEquals("TXN001", transactions.get(0).getTransactionNumber(), "El número de transacción debería ser TXN001");
    }
}
