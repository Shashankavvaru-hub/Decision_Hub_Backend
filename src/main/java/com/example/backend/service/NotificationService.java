package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.NotificationDto;
import com.example.backend.entity.Community;
import com.example.backend.entity.Decision;
import com.example.backend.entity.Notification;
import com.example.backend.entity.NotificationType;
import com.example.backend.entity.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.exception.ResourceNotFoundException;
//import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedActionException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Generic notification creator
     */
    public NotificationDto createNotification(
            User receiver,
            User sender,
            NotificationType type,
            String title,
            String message,
            Decision decision,
            Community community,
            Long referenceId) {

        // Don't notify yourself
        if (receiver != null &&
            sender != null &&
            receiver.getId().equals(sender.getId())) {
            return null;
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(type)
                .title(title)
                .message(message)
                .decision(decision)
                .community(community)
                .referenceId(referenceId)
                .build();

        Notification saved = notificationRepository.save(notification);

        return convertToDto(saved);
    }

    /**
     * Get all notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getAllNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByReceiverIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Count unread notifications
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByReceiverIdAndReadFalse(userId);
    }

    /**
     * Mark one notification as read
     */
    public void markAsRead(Long notificationId, User user) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found."));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new UnauthorizedActionException(
                    "You cannot modify another user's notification.");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }
    /**
     * Mark all notifications as read
     */
    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository.findByReceiverIdAndReadFalseOrderByCreatedAtDesc(userId);

        notifications.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId, User user) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found."));

        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new UnauthorizedActionException(
                    "You cannot delete another user's notification.");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Entity → DTO
     */
    private NotificationDto convertToDto(Notification notification) {

        return NotificationDto.builder()
                .id(notification.getId())

                .senderId(notification.getSender() != null
                        ? notification.getSender().getId()
                        : null)

                .senderUsername(notification.getSender() != null
                        ? notification.getSender().getUsername()
                        : "System")

                .type(notification.getType())

                .title(notification.getTitle())

                .message(notification.getMessage())

                .decisionId(notification.getDecision() != null
                        ? notification.getDecision().getId()
                        : null)

                .communityId(notification.getCommunity() != null
                        ? notification.getCommunity().getId()
                        : null)

                .referenceId(notification.getReferenceId())

                .read(notification.isRead())

                .createdAt(notification.getCreatedAt())

                .build();
    }
}