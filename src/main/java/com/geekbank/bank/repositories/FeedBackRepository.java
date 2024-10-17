package com.geekbank.bank.repositories;

import com.geekbank.bank.models.Feedback;
import com.geekbank.bank.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Date;
import java.util.Optional;
import java.util.List;

@Repository
public interface FeedBackRepository extends JpaRepository<Feedback, Long>{


    List<Feedback> findByGiftCardId(Long giftCardId);
//    List<Feedback> findByUser(User user);

    // Encuentra feedbacks por userId
    List<Feedback> findByUserId(Long userId);

    // Encuentra feedbacks creados después de una fecha específica
    List<Feedback> findByCreatedAtAfter(Date date);

    // Encuentra feedbacks creados antes de una fecha específica
    List<Feedback> findByCreatedAtBefore(Date date);

    // Encuentra feedbacks dentro de un rango de fechas
    List<Feedback> findByCreatedAtBetween(Date startDate, Date endDate);

    // Método personalizado usando @Query para una consulta más específica
    @org.springframework.data.jpa.repository.Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findFeedbacksInDateRange(@org.springframework.data.repository.query.Param("startDate") Date startDate,
                                            @org.springframework.data.repository.query.Param("endDate") Date endDate);


}
