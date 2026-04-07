package com.courseflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.courseflow.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}