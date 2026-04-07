package com.courseflow.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.courseflow.dto.LoginResponse;
import com.courseflow.dto.RegisterRequest;
import com.courseflow.dto.UserResponse;
import com.courseflow.model.User;
import com.courseflow.repository.UserRepository;
import com.courseflow.security.JwtService;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse signup(RegisterRequest request) {
        repo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new RuntimeException("Email already exists");
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));
        user.setStatus("Approved");
        user.setJoinedAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());

        return toResponse(repo.save(user));
    }

    public LoginResponse login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        if ("Blocked".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("User is blocked");
        }

        user.setLastActiveAt(LocalDateTime.now());
        User savedUser = repo.save(user);
        String token = jwtService.generateToken(savedUser);

        LoginResponse response = new LoginResponse();
        copyUserFields(savedUser, response);
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresAt(jwtService.extractExpiration(token));
        return response;
    }

    public List<UserResponse> getAllUsers() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateStatus(Long id, String status) {
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(normalizeStatus(status));
        return toResponse(repo.save(user));
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "student";
        }
        return role.trim().toLowerCase();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }
        if ("approved".equalsIgnoreCase(status)) {
            return "Approved";
        }
        if ("blocked".equalsIgnoreCase(status)) {
            return "Blocked";
        }
        throw new RuntimeException("Invalid status");
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        copyUserFields(user, response);
        return response;
    }

    private void copyUserFields(User user, UserResponse response) {
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setJoinedAt(user.getJoinedAt());
        response.setLastActiveAt(user.getLastActiveAt());
    }
}
