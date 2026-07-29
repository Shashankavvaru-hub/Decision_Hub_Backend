package com.example.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.backend.dto.NotificationDto;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedActionException;
import com.example.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDto createVoteNotification(Decision decision, User voter, Long voteId) {
        return create(decision.getUser(), voter, NotificationType.VOTE,
                displayName(voter) + " voted on your decision \"" + decision.getTitle() + "\".",
                decision, voteId);
    }

    @Transactional
    public NotificationDto createCommentNotification(Decision decision, User commenter, Long commentId) {
        return create(decision.getUser(), commenter, NotificationType.COMMENT,
                displayName(commenter) + " commented on your decision \"" + decision.getTitle() + "\".",
                decision, commentId);
    }

    @Transactional
    public NotificationDto createInvitationNotification(Decision decision, User inviter, User invitee, Long invitationId) {
        return create(invitee, inviter, NotificationType.INVITATION,
                displayName(inviter) + " invited you to the decision \"" + decision.getTitle() + "\".",
                decision, invitationId);
    }

    private NotificationDto create(User receiver, User sender, NotificationType type,
                                   String message, Decision decision, Long referenceId) {
        if (receiver == null) throw new IllegalArgumentException("Notification receiver is required");
        if (sender != null && receiver.getId().equals(sender.getId())) return null;
        Notification saved = notificationRepository.save(Notification.builder()
                .receiver(receiver).sender(sender).type(type).message(message)
                .decision(decision).referenceId(referenceId).read(false).build());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getAll(User user) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnread(User user) {
        return notificationRepository.findByReceiverIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countByReceiverIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationDto markRead(Long id, User user) {
        Notification notification = owned(id, user);
        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(User user) {
        List<Notification> items = notificationRepository
                .findByReceiverIdAndReadFalseOrderByCreatedAtDesc(user.getId());
        items.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(items);
    }

    @Transactional
    public void delete(Long id, User user) {
        notificationRepository.delete(owned(id, user));
    }

    private Notification owned(Long id, User user) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        if (!n.getReceiver().getId().equals(user.getId()))
            throw new UnauthorizedActionException("You cannot access another user's notification.");
        return n;
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .senderId(n.getSender() == null ? null : n.getSender().getId())
                .senderUsername(n.getSender() == null ? null : displayName(n.getSender()))
                .type(n.getType()).message(n.getMessage())
                .decisionId(n.getDecision() == null ? null : n.getDecision().getId())
                .referenceId(n.getReferenceId()).read(n.isRead()).createdAt(n.getCreatedAt())
                .build();
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getActualUsername();
    }
}
