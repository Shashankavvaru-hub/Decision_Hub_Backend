package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;

import com.example.backend.dto.CommunityDto;
import com.example.backend.dto.CommunityJoinRequestDto;
import com.example.backend.dto.CreateCommunityRequest;
import com.example.backend.dto.HandleJoinRequestDto;
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

import com.example.backend.dto.CommunityMemberDto;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommunityDto>> createCommunity(@Valid @RequestBody CreateCommunityRequest request, 
                                                        @AuthenticationPrincipal User user) {
        CommunityDto community = communityService.createCommunity(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.<CommunityDto>builder()
                .success(true)
                .message("Community created successfully.")
                .data(community)
                .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunityDto>>> getAllCommunities() {
        List<CommunityDto> communities = communityService.getAllCommunities();
        String message = communities.isEmpty() ? "No communities found." : "Communities fetched successfully.";
        return ResponseEntity.ok(
            ApiResponse.<List<CommunityDto>>builder()
                .success(true)
                .message(message)
                .data(communities)
                .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommunityDto>> getCommunityById(@PathVariable Long id) {
        CommunityDto community = communityService.getCommunityById(id);
        return ResponseEntity.ok(
            ApiResponse.<CommunityDto>builder()
                .success(true)
                .message("Community fetched successfully.")
                .data(community)
                .build()
        );
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<String>> joinCommunity(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        String result = communityService.joinCommunity(id, user);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("Join request processed.").data(result).build());
    }

    @GetMapping("/{id}/requests")
    public ResponseEntity<ApiResponse<List<CommunityJoinRequestDto>>> getPendingRequests(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<CommunityJoinRequestDto> requests = communityService.getPendingRequests(id, user);
        return ResponseEntity.ok(ApiResponse.<List<CommunityJoinRequestDto>>builder().success(true).message("Pending requests fetched.").data(requests).build());
    }

    @PostMapping("/requests/{requestId}/handle")
    public ResponseEntity<ApiResponse<String>> handleJoinRequest(
            @PathVariable Long requestId,
            @RequestBody HandleJoinRequestDto handleRequestDto,
            @AuthenticationPrincipal User user) {
        String result = communityService.handleJoinRequest(requestId, handleRequestDto.isAccept(), user);
        return ResponseEntity.ok(ApiResponse.<String>builder().success(true).message("Join request handled.").data(result).build());
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<?>> removeMember(@PathVariable Long id, @PathVariable Long userId, @AuthenticationPrincipal User user) {
        communityService.removeMember(id, userId, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member removed successfully."));
    }

    
    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CommunityMemberDto>>> getCommunityMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        List<CommunityMemberDto> members =
                communityService.getCommunityMembers(id, user);

        return ResponseEntity.ok(
                ApiResponse.<List<CommunityMemberDto>>builder()
                        .success(true)
                        .message("Community members fetched successfully.")
                        .data(members)
                        .build());
    }
    
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteCommunity(@PathVariable Long id, @AuthenticationPrincipal User user) {
        communityService.deleteCommunity(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Community deleted successfully."));
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countCommunities() {
        return ResponseEntity.ok(
            ApiResponse.<Long>builder()
                .success(true)
                .message("Community count fetched successfully.")
                .data(communityService.countCommunities())
                .build()
        );
    }
}
