package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.dto.DecisionRequest;
import com.example.backend.entity.Decision;
import com.example.backend.repository.DecisionRepository;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;

    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    public Decision createDecision(DecisionRequest request) {

        Decision decision = new Decision();

        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());
        decision.setCreatedAt(LocalDateTime.now());

        return decisionRepository.save(decision);
    }

    public List<Decision> getAllDecisions() {

        return decisionRepository.findAll();
    }

    public Decision getDecisionById(Long id) {

        return decisionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Decision not found!"));
    }

    public Decision updateDecision(Long id, DecisionRequest request) {

        Decision decision = getDecisionById(id);

        decision.setTitle(request.getTitle());
        decision.setDescription(request.getDescription());

        return decisionRepository.save(decision);
    }

    public void deleteDecision(Long id) {

        Decision decision = getDecisionById(id);

        decisionRepository.delete(decision);
    }
}