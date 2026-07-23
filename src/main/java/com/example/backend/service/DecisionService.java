package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedActionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.DecisionRequest;
import com.example.backend.dto.OptionRequest;
import com.example.backend.dto.DecisionDto;
import com.example.backend.dto.OptionDto;
import com.example.backend.entity.Decision;
import com.example.backend.entity.Option;
import com.example.backend.entity.Role;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.CommunityMemberRepository;
import com.example.backend.repository.CommunityRepository;
import com.example.backend.repository.DecisionRepository;
import com.example.backend.repository.VoteRepository;
import com.example.backend.entity.Vote;
import java.util.Optional;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final VoteRepository voteRepository;

    public DecisionService(DecisionRepository decisionRepository,
                           CommunityRepository communityRepository,
                           CommunityMemberRepository communityMemberRepository,
                           VoteRepository voteRepository) {
        this.decisionRepository = decisionRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public DecisionDto createDecision(
            DecisionRequest request,
            User user) {

        Decision decision = new Decision();

        decision.setUser(user);
        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCategory(request.getCategory());

        if (request.getCommunityId() != null) {
            Community community = communityRepository.findById(request.getCommunityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community not found."));

            boolean isModerator = community.getModerator().getId().equals(user.getId());
            boolean isMember = communityMemberRepository.existsByCommunityIdAndUserId(community.getId(), user.getId());
            if (!isModerator && !isMember && user.getRole() != Role.ADMIN) {
                throw new UnauthorizedActionException("Only members of the community can create a decision for this community.");
            }
            decision.setCommunity(community);
        }

        List<Option> options = new ArrayList<>();
        if (request.getOptions() != null) {
            for (OptionRequest optReq : request.getOptions()) {
                Option option = new Option();
                option.setDecision(decision);
                option.setOptionTitle(optReq.getOptionTitle());
                option.setDescription(optReq.getDescription());
                option.setPros(optReq.getPros());
                option.setCons(optReq.getCons());
                options.add(option);
            }
        }
        decision.setOptions(options);

        Decision saved = decisionRepository.save(decision);
        return convertToDto(saved, user);
    }

    @Transactional(readOnly = true)
    public List<DecisionDto> getAllDecisions(User requester) {
        return decisionRepository.findAll().stream()
                .filter(decision -> hasAccess(decision, requester))
                .map(decision -> convertToDto(decision, requester))
                .collect(Collectors.toList());
    }

    private boolean hasAccess(Decision decision, User user) {
        if (decision.getCommunity() == null) return true;
        if (user.getRole() == Role.ADMIN) return true;
        return communityMemberRepository.existsByCommunityIdAndUserId(decision.getCommunity().getId(), user.getId());
    }

    @Transactional(readOnly = true)
    public Decision getDecisionEntityById(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Decision not found!"));
    }

    @Transactional(readOnly = true)
    public DecisionDto getDecisionById(Long id, User requester) {
        Decision decision = getDecisionEntityById(id);
        if (!hasAccess(decision, requester)) {
            throw new UnauthorizedActionException("You do not have access to view this decision.");
        }
        return convertToDto(decision, requester);
    }

    @Transactional
    public DecisionDto updateDecision(
            Long id,
            DecisionRequest request,
            User requester) {

        Decision decision = getDecisionEntityById(id);

        boolean isOwner =
                decision.getUser().getId()
                        .equals(requester.getId());

        boolean isAdmin =
                requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException(
                    "Only the decision owner or admin can update this decision.");
        }

        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCategory(request.getCategory());

        Decision saved = decisionRepository.save(decision);
        return convertToDto(saved, requester);
    }

    @Transactional
    public void deleteDecision(
            Long id,
            User requester) {

        Decision decision = getDecisionEntityById(id);

        boolean isOwner =
                decision.getUser().getId()
                        .equals(requester.getId());

        boolean isAdmin =
                requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedActionException(
                    "Only the decision owner or admin can delete this decision.");
        }

        if (decision.getVotes() != null && !decision.getVotes().isEmpty()) {
            voteRepository.deleteAll(decision.getVotes());
            decision.getVotes().clear();
        }

        decisionRepository.delete(decision);
    }

    @Transactional(readOnly = true)
    public long countDecisions() {
        return decisionRepository.count();
    }

    private DecisionDto convertToDto(Decision decision, User requester) {
        DecisionDto dto = new DecisionDto();
        dto.setId(decision.getId());
        dto.setUserId(decision.getUser().getId());
        dto.setTitle(decision.getTitle());
        dto.setDescription(decision.getDescription());
        dto.setCategory(decision.getCategory());
        
        if (requester != null) {
            Optional<Vote> voteOpt = voteRepository.findByDecisionIdAndUserId(decision.getId(), requester.getId());
            if (voteOpt.isPresent()) {
                dto.setVotedOptionId(voteOpt.get().getOption().getId());
            }
        }
        
        if (decision.getCommunity() != null) {
            dto.setCommunityId(decision.getCommunity().getId());
            dto.setCommunityName(decision.getCommunity().getName());
        }
        
        dto.setStatus(decision.getStatus());
        dto.setVisibility(decision.getVisibility());
        dto.setCreatedAt(decision.getCreatedAt());
        dto.setUpdatedAt(decision.getUpdatedAt());

        if (decision.getOptions() != null) {
            dto.setOptions(decision.getOptions().stream().map(opt -> {
                OptionDto optDto = new OptionDto();
                optDto.setId(opt.getId());
                optDto.setDecisionId(decision.getId());
                optDto.setOptionTitle(opt.getOptionTitle());
                optDto.setDescription(opt.getDescription());
                optDto.setPros(opt.getPros());
                optDto.setCons(opt.getCons());
                optDto.setScore(opt.getScore());
                optDto.setRanking(opt.getRanking());
                optDto.setCreatedAt(opt.getCreatedAt());
                return optDto;
            }).collect(Collectors.toList()));
        } else {
            dto.setOptions(new ArrayList<>());
        }

        return dto;
    }
}