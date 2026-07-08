package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createCommunity() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllCommunities() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommunityById(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> joinCommunity(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }
}
