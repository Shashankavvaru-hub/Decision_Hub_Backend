package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal User user) {
        UserDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(
            ApiResponse.<UserDto>builder()
                .success(true)
                .message("User details fetched successfully.")
                .data(userDto)
                .build()
        );
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countUsers() {
        return ResponseEntity.ok(
            ApiResponse.<Long>builder()
                .success(true)
                .message("User count fetched successfully.")
                .data(userService.countUsers())
                .build()
        );
    }
}
