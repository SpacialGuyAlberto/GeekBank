// src/main/java/com/geekbank/bank/repositories/CartItemRepository.java
package com.geekbank.bank.repositories;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}
