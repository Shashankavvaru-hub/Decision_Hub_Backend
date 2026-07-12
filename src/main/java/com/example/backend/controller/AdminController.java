package com.example.backend.controller;

import java.util.List;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.ApiResponse;
import com.example.backend.entity.User;
import com.example.backend.service.CommunityService;
import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(
            ApiResponse.<List<UserDto>>builder()
                .success(true)
                .message("Users fetched successfully.")
                .data(users)
                .build()
        );
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User status updated successfully."));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUserRole(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "User role updated successfully."));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully."));
    }

    @DeleteMapping("/communities/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteCommunity(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.deleteCommunity(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Community deleted successfully."));
    }

    @DeleteMapping("/boards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteBoard(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Board deleted successfully."));
    }

    @GetMapping("/metrics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getMetricsSummary() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Metrics summary fetched successfully."));
    }
}
