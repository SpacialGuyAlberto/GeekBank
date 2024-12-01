package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Orders;
import com.geekbank.bank.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByOrderRequestId(String orderRequestId);

    List<Orders> findByUserId(Long userId);

    List<Orders> findByGuestId(String guestId);

    List<Orders> findByGameUserId(Long gameUserId);

    List<Orders> findByManual(Boolean manual);

    List<Orders> findByPhoneNumber(String phoneNumber);

    Optional<Orders> findByRefNumber(String refNumber);

    List<Orders> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Orders findByTransaction_Id(Long transactionId);

    List<Orders> findByProductsContains(Product product);

    boolean existsByOrderRequestId(String orderRequestId);
    boolean existsByRefNumber(String refNumber);

    boolean existsByUserId(Long userId);
}
