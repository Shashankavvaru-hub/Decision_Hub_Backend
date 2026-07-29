package com.example.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    List<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(Long receiverId);
    long countByReceiverIdAndReadFalse(Long receiverId);
}
