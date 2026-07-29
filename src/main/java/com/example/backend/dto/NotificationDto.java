package com.example.backend.dto;

import java.time.LocalDateTime;
import com.example.backend.entity.NotificationType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private NotificationType type;
    private String message;
    private Long decisionId;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
