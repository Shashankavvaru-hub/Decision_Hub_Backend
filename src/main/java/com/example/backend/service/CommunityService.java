package com.example.backend.service;

import com.example.backend.dto.CommunityDto;
import com.example.backend.dto.CreateCommunityRequest;
import com.example.backend.entity.Community;
import com.example.backend.entity.CommunityMember;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceAlreadyExistsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedActionException;
import com.example.backend.repository.CommunityMemberRepository;
import com.example.backend.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;

    @Transactional
    public CommunityDto createCommunity(CreateCommunityRequest request, User creator) {
        if (communityRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Community name already exists.");
        }

        Community community = Community.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .moderator(creator)
                .memberCount(1) // Creator is the first member
                .build();

        community = communityRepository.save(community);

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(creator)
                .memberRole("MODERATOR")
                .build();

        communityMemberRepository.save(member);

        return convertToDto(community);
    }

    @Transactional(readOnly = true)
    public List<CommunityDto> getAllCommunities() {
        return communityRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommunityDto getCommunityById(Long id) {
        Community community = getCommunityEntity(id);
        return convertToDto(community);
    }

    @Transactional
    public void joinCommunity(Long communityId, User user) {
        Community community = getCommunityEntity(communityId);

        if (communityMemberRepository.existsByCommunityIdAndUserId(community.getId(), user.getId())) {
            throw new ResourceAlreadyExistsException("User is already a member of this community.");
        }

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(user)
                .memberRole("MEMBER")
                .build();

        communityMemberRepository.save(member);
        
        community.setMemberCount(community.getMemberCount() + 1);
        communityRepository.save(community);
    }

    @Transactional
    public void removeMember(Long communityId, Long userId, User requester) {
        Community community = getCommunityEntity(communityId);

        boolean isModerator = community.getModerator().getId().equals(requester.getId());
        boolean isSelf = requester.getId().equals(userId);
        boolean targetIsModerator = community.getModerator().getId().equals(userId);

        if (!isModerator && !isSelf) {
            throw new UnauthorizedActionException("You are not authorized to remove this member.");
        }

        if (targetIsModerator) {
            throw new BadRequestException("Community owner cannot be removed or leave without transferring ownership or deleting the community.");
        }

        CommunityMember member = communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this community."));

        communityMemberRepository.delete(member);

        community.setMemberCount(Math.max(0, community.getMemberCount() - 1));
        communityRepository.save(community);
    }

    @Transactional
    public void deleteCommunity(Long communityId, User requester) {
        Community community = getCommunityEntity(communityId);

        boolean isModerator = community.getModerator().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isModerator && !isAdmin) {
            throw new UnauthorizedActionException("Only the community owner or admin can delete the community.");
        }

        communityRepository.delete(community);
    }

    private Community getCommunityEntity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community not found."));
    }

    private CommunityDto convertToDto(Community community) {
        return CommunityDto.builder()
                .id(community.getId())
                .name(community.getName())
                .category(community.getCategory())
                .description(community.getDescription())
                .moderatorId(community.getModerator().getId())
                .moderatorUsername(community.getModerator().getActualUsername())
                .memberCount(community.getMemberCount())
                .createdAt(community.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public long countCommunities() {
        return communityRepository.count();
    }
}
