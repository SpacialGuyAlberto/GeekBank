package com.geekbank.bank.repositories;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProductId(User user, Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.productId = :id")
    void deleteByCustomQuery(Long id);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user = :user")
    void deleteAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.productId = :id AND c.user = :user")
    void updateQuantityByProductIdAndUser(Long id, int quantity, User user);
}
