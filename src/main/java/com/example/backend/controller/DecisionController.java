package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DecisionRequest;
import com.example.backend.entity.Decision;
import com.example.backend.entity.User;
import com.example.backend.service.DecisionService;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Decision>> createDecision(
            @RequestBody DecisionRequest request,
            @AuthenticationPrincipal User user) {

        Decision decision =
                decisionService.createDecision(request, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<Decision>builder()
                        .success(true)
                        .message("Decision created successfully.")
                        .data(decision)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<Decision>>> getAllDecisions() {

        List<Decision> decisions = decisionService.getAllDecisions();
        String message = decisions.isEmpty() ? "No decisions found." : "Decisions fetched successfully.";

        return ResponseEntity.ok(ApiResponse.<List<Decision>>builder()
                .success(true)
                .message(message)
                .data(decisions)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Decision>> getDecisionById(
            @PathVariable Long id) {

        Decision decision = decisionService.getDecisionById(id);

        return ResponseEntity.ok(ApiResponse.<Decision>builder()
                .success(true)
                .message("Decision fetched successfully.")
                .data(decision)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Decision>> updateDecision(
            @PathVariable Long id,
            @RequestBody DecisionRequest request,
            @AuthenticationPrincipal User user) {

        Decision decision = decisionService.updateDecision(
                        id,
                        request,
                        user);

        return ResponseEntity.ok(ApiResponse.<Decision>builder()
                .success(true)
                .message("Decision updated successfully.")
                .data(decision)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteDecision(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        decisionService.deleteDecision(id, user);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Decision deleted successfully!"));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countDecisions() {
        return ResponseEntity.ok(
            ApiResponse.<Long>builder()
                .success(true)
                .message("Decision count fetched successfully.")
                .data(decisionService.countDecisions())
                .build()
        );
    }
}