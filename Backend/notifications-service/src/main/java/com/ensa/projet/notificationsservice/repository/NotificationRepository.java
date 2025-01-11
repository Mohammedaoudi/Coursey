package com.ensa.projet.notificationsservice.repository;


import com.ensa.projet.notificationsservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserIdAndReadedFalseOrderByCreatedAtDesc(Integer userId);
    Long countByUserIdAndReadedFalse(Integer userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readed = true WHERE n.userId = :userId")
    void markAllAsRead(Integer userId);
}