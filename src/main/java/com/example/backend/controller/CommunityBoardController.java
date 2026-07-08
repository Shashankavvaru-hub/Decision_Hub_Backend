package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/communities/{id}/boards")
public class CommunityBoardController {

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createBoard(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBoardsForCommunity(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
}
