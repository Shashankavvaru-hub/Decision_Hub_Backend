package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.VoteDto;
import com.example.backend.dto.VoteRequest;
import com.example.backend.entity.Decision;
import com.example.backend.entity.Option;
import com.example.backend.entity.User;
import com.example.backend.entity.Vote;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.DecisionRepository;
import com.example.backend.repository.OptionRepository;
import com.example.backend.repository.VoteRepository;

import java.util.Optional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final OptionRepository optionRepository;
    private final DecisionRepository decisionRepository;

    public VoteService(VoteRepository voteRepository, OptionRepository optionRepository, DecisionRepository decisionRepository) {
        this.voteRepository = voteRepository;
        this.optionRepository = optionRepository;
        this.decisionRepository = decisionRepository;
    }

    @Transactional
    public VoteDto castVote(Long decisionId, VoteRequest request, User user) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision not found"));

        Option newOption = optionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));

        if (!newOption.getDecision().getId().equals(decision.getId())) {
            throw new BadRequestException("Option does not belong to the specified decision");
        }

        Optional<Vote> existingVoteOpt = voteRepository.findByDecisionIdAndUserId(decision.getId(), user.getId());

        if (existingVoteOpt.isPresent()) {
            Vote existingVote = existingVoteOpt.get();
            Option oldOption = existingVote.getOption();

            if (!oldOption.getId().equals(newOption.getId())) {
                // Changing vote
                oldOption.setScore(oldOption.getScore() - 1);
                optionRepository.save(oldOption);

                existingVote.setOption(newOption);
                existingVote.setVoteType(request.getVoteType());
                
                newOption.setScore(newOption.getScore() + 1);
                optionRepository.save(newOption);

                return convertToDto(voteRepository.save(existingVote));
            } else {
                // Same option, maybe updating voteType
                existingVote.setVoteType(request.getVoteType());
                return convertToDto(voteRepository.save(existingVote));
            }
        } else {
            // New vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setDecision(decision);
            vote.setOption(newOption);
            vote.setVoteType(request.getVoteType());

            newOption.setScore(newOption.getScore() + 1);
            optionRepository.save(newOption);

            return convertToDto(voteRepository.save(vote));
        }
    }

    private VoteDto convertToDto(Vote vote) {
        VoteDto dto = new VoteDto();
        dto.setId(vote.getId());
        dto.setUserId(vote.getUser().getId());
        dto.setDecisionId(vote.getDecision().getId());
        dto.setOptionId(vote.getOption().getId());
        dto.setVoteType(vote.getVoteType());
        dto.setCreatedAt(vote.getCreatedAt());
        return dto;
    }
}
