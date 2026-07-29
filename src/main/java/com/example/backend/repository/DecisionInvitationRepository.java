package com.example.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.DecisionInvitation;

public interface DecisionInvitationRepository extends JpaRepository<DecisionInvitation, Long> {
    boolean existsByDecisionIdAndInviteeId(Long decisionId, Long inviteeId);
    List<DecisionInvitation> findByInviteeIdOrderByCreatedAtDesc(Long inviteeId);
}
