package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Decision> createDecision(
            @RequestBody DecisionRequest request,
            @AuthenticationPrincipal User user) {

        Decision decision =
                decisionService.createDecision(request, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(decision);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Decision>> getAllDecisions() {

        return ResponseEntity.ok(
                decisionService.getAllDecisions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Decision> getDecisionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                decisionService.getDecisionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Decision> updateDecision(
            @PathVariable Long id,
            @RequestBody DecisionRequest request,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
                decisionService.updateDecision(
                        id,
                        request,
                        user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> deleteDecision(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        decisionService.deleteDecision(id, user);

        return ResponseEntity.ok(
                "Decision deleted successfully!");
    }
}