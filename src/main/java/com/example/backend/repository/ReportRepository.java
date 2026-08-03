package com.example.backend.repository;

import com.example.backend.entity.Report;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    List<Report> findByTargetTypeOrderByCreatedAtDesc(String targetType);

    List<Report> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.reporter = :user OR r.reportedUser = :user OR r.moderator = :user")
    void deleteByUserReference(@Param("user") User user);
}
