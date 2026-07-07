package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.DecisionRequest;
import com.example.backend.entity.Decision;
import com.example.backend.service.DecisionService;

@RestController
@RequestMapping("/api/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping
    public ResponseEntity<Decision> createDecision(
            @RequestBody DecisionRequest request) {

        Decision decision = decisionService.createDecision(request);

        return ResponseEntity.ok(decision);
    }

    @GetMapping
    public ResponseEntity<List<Decision>> getAllDecisions() {

        return ResponseEntity.ok(
                decisionService.getAllDecisions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Decision> getDecisionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                decisionService.getDecisionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Decision> updateDecision(
            @PathVariable Long id,
            @RequestBody DecisionRequest request) {

        return ResponseEntity.ok(
                decisionService.updateDecision(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDecision(
            @PathVariable Long id) {

        decisionService.deleteDecision(id);

        return ResponseEntity.ok(
                "Decision deleted successfully!");
    }
}