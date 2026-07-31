package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications of a user (Newest first)
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // Only unread notifications
    List<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(Long receiverId);

    // Count unread notifications
    long countByReceiverIdAndReadFalse(Long receiverId);
}