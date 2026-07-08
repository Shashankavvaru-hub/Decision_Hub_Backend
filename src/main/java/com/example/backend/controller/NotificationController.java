package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getNotifications() {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
