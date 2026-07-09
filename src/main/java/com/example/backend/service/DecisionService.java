package com.example.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.DecisionRequest;
import com.example.backend.entity.Decision;
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
    public Decision createDecision(
            DecisionRequest request,
            User user) {

        Decision decision = new Decision();

        decision.setUser(user);
        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCategory(request.getCategory());

        return decisionRepository.save(decision);
    }

    @Transactional(readOnly = true)
    public List<Decision> getAllDecisions() {
        return decisionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Decision getDecisionById(Long id) {

        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Decision not found!"));
    }

    @Transactional
    public Decision updateDecision(
            Long id,
            DecisionRequest request,
            User requester) {

        Decision decision = getDecisionById(id);

        boolean isOwner =
                decision.getUser().getId()
                        .equals(requester.getId());

        if (!isOwner) {
            throw new AccessDeniedException(
                    "Only the decision owner can update this decision.");
        }

        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCategory(request.getCategory());

        return decisionRepository.save(decision);
    }

    @Transactional
    public void deleteDecision(
            Long id,
            User requester) {

        Decision decision = getDecisionById(id);

        boolean isOwner =
                decision.getUser().getId()
                        .equals(requester.getId());

        boolean isAdmin =
                requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "Only the decision owner or admin can delete this decision.");
        }

        decisionRepository.delete(decision);
    }
}