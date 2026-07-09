package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> getNotifications() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications fetched successfully."));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> markNotificationAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read successfully."));
    }
}
