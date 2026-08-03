package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Notification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	@Modifying
	@Query("""
	       UPDATE Notification n
	       SET n.community = null
	       WHERE n.community.id = :communityId
	       """)
	void clearCommunityReference(@Param("communityId") Long communityId);

	@Modifying
	@Query("""
	       UPDATE Notification n
	       SET n.decision = null
	       WHERE n.decision.id = :decisionId
	       """)
	void clearDecisionReference(@Param("decisionId") Long decisionId);

    // All notifications of a user (Newest first)
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // Only unread notifications
    List<Notification> findByReceiverIdAndReadFalseOrderByCreatedAtDesc(Long receiverId);

    // Count unread notifications
    long countByReceiverIdAndReadFalse(Long receiverId);
}