package com.geekbank.bank.payment.stripe.repository;

import com.geekbank.bank.payment.stripe.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>{
}
