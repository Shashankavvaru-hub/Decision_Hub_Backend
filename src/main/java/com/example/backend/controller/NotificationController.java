package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<NotificationDto>>builder().success(true)
                .message("Notifications fetched successfully.").data(notificationService.getAll(user)).build());
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnread(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<List<NotificationDto>>builder().success(true)
                .message("Unread notifications fetched successfully.").data(notificationService.getUnread(user)).build());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder().success(true)
                .message("Unread count fetched successfully.")
                .data(Map.of("unreadCount", notificationService.unreadCount(user))).build());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.<NotificationDto>builder().success(true)
                .message("Notification marked as read.").data(notificationService.markRead(id, user)).build());
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true)
                .message("All notifications marked as read.").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true)
                .message("Notification deleted successfully.").build());
    }
}
