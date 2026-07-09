package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Decision;

public interface DecisionRepository extends JpaRepository<Decision, Long> {

}