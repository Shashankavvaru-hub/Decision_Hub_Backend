package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBoardById(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateBoardStatus(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/votes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> voteOnBoard(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/votes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeVoteFromBoard(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
