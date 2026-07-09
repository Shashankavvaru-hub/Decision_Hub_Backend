package com.example.backend.repository;

import com.example.backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    boolean existsByName(String name);
}
