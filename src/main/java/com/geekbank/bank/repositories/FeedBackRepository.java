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

    List<Feedback> findByUser(User user);

    // Alternativamente, si quieres buscar por el ID del usuario
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findAll();
    Optional<Feedback> findById(Long id);
    List<Feedback> findByProductId(String productId);

    List<Feedback> findByCreatedAtAfter(Date date);
    List<Feedback> findByUserAndProductId(User user, String productId);

    // Alternativamente, si quieres buscar por el ID del usuario
    List<Feedback> findByUserIdAndProductId(Long userId, String productId);

    // Encuentra feedbacks creados antes de una fecha específica
    List<Feedback> findByCreatedAtBefore(Date date);

    // Encuentra feedbacks dentro de un rango de fechas
    List<Feedback> findByCreatedAtBetween(Date startDate, Date endDate);

    // Otra forma usando @Query para una consulta más personalizada
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findFeedbacksInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


}
