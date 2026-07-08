package com.example.backend.controller;

import com.example.backend.dto.CommunityDto;
import com.example.backend.dto.CreateCommunityRequest;
import com.example.backend.entity.User;
import com.example.backend.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommunityDto> createCommunity(@Valid @RequestBody CreateCommunityRequest request, 
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createCommunity(request, user));
    }

    @GetMapping
    public ResponseEntity<List<CommunityDto>> getAllCommunities() {
        return ResponseEntity.ok(communityService.getAllCommunities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDto> getCommunityById(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getCommunityById(id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> joinCommunity(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.joinCommunity(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        communityService.removeMember(id, userId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCommunity(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.deleteCommunity(id, user);
        return ResponseEntity.ok().build();
    }
}
