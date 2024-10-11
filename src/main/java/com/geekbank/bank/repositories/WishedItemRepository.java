package com.geekbank.bank.repositories;

import com.geekbank.bank.models.WishedItem;
import com.geekbank.bank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WishedItemRepository extends JpaRepository<WishedItem, Long>{

    List<WishedItem> findByUser(User user);
    WishedItem findByUserAndProductId(User user, Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM WishedItem c WHERE c.productId = :id")
    void deleteByCustomQuery(Long id);

    @Transactional
    @Modifying
    @Query("DELETE FROM WishedItem c WHERE c.user = :user")
    void deleteAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE WishedItem c SET c.quantity = :quantity WHERE c.productId = :id AND c.user = :user")
    void updateQuantityByProductIdAndUser(Long id, int quantity, User user);

}
