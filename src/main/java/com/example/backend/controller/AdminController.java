package com.example.backend.controller;

import java.util.List;
import com.example.backend.dto.UserDto;
import com.example.backend.service.CommunityService;
import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CommunityService communityService;

    public AdminController(UserService userService, CommunityService communityService) {
        this.userService = userService;
        this.communityService = communityService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/communities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id, @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.entity.User user) {
        communityService.deleteCommunity(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/boards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoard(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/metrics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMetricsSummary() {
        return ResponseEntity.ok().build();
    }
}
