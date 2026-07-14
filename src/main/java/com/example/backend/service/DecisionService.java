package com.example.backend.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

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
import com.example.backend.entity.User;
import com.example.backend.repository.DecisionRepository;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;

    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
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
        return convertToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DecisionDto> getAllDecisions() {
        return decisionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Decision getDecisionEntityById(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Decision not found!"));
    }

    @Transactional(readOnly = true)
    public DecisionDto getDecisionById(Long id) {
        return convertToDto(getDecisionEntityById(id));
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

        if (!isOwner) {
            throw new UnauthorizedActionException(
                    "Only the decision owner can update this decision.");
        }

        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCategory(request.getCategory());

        Decision saved = decisionRepository.save(decision);
        return convertToDto(saved);
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

        decisionRepository.delete(decision);
    }

    @Transactional(readOnly = true)
    public long countDecisions() {
        return decisionRepository.count();
    }

    private DecisionDto convertToDto(Decision decision) {
        DecisionDto dto = new DecisionDto();
        dto.setId(decision.getId());
        dto.setUserId(decision.getUser().getId());
        dto.setTitle(decision.getTitle());
        dto.setDescription(decision.getDescription());
        dto.setCategory(decision.getCategory());
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