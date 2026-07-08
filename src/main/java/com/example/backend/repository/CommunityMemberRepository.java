package com.example.backend.repository;

import com.example.backend.entity.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {
    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);
    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);
    void deleteByCommunityIdAndUserId(Long communityId, Long userId);
}
