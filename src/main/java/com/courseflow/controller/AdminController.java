package com.courseflow.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.courseflow.dto.RegistrationDecisionRequest;
import com.courseflow.dto.RegistrationResponse;
import com.courseflow.dto.UserResponse;
import com.courseflow.dto.UserStatusUpdateRequest;
import com.courseflow.service.RegistrationService;
import com.courseflow.service.UserService;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    private final RegistrationService registrationService;
    private final UserService userService;

    public AdminController(RegistrationService registrationService, UserService userService) {
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id,
                                                         @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RegistrationResponse>> getPending() {
        return ResponseEntity.ok(registrationService.getPending());
    }

    @PatchMapping("/approve/{id}")
    public ResponseEntity<RegistrationResponse> approve(@PathVariable Long id,
                                                        @RequestBody(required = false) RegistrationDecisionRequest request) {
        return ResponseEntity.ok(registrationService.approve(id, request));
    }

    @PatchMapping("/reject/{id}")
    public ResponseEntity<RegistrationResponse> reject(@PathVariable Long id,
                                                       @RequestBody(required = false) RegistrationDecisionRequest request) {
        return ResponseEntity.ok(registrationService.reject(id, request));
    }
}
