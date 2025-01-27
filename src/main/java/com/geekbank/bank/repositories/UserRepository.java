package com.geekbank.bank.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.geekbank.bank.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);

    User findByAffiliateLink(String affiliateLink);
    User findByPromoCode(String promoCode);
    Optional<User> findByActivationToken(String activationToken);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.account WHERE u.id = :userId")
    Optional<User> findByIdWithAccount(@Param("userId") Long userId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.account")
    List<User> findAllWithAccount();
}
