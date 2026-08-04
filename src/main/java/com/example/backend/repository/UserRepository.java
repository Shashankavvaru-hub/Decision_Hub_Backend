package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findByRole(Role role);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    long countByStatus(String status);
}