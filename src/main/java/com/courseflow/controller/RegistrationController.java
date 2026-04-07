package com.courseflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courseflow.dto.PagedResponse;
import com.courseflow.dto.RegistrationAggregateResponse;
import com.courseflow.dto.RegistrationResponse;
import com.courseflow.service.RegistrationService;

@RestController
@RequestMapping("/registrations")
@CrossOrigin
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(@RequestParam Long userId,
                                                         @RequestParam Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(userId, courseId));
    }

    @GetMapping
    public ResponseEntity<List<RegistrationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<PagedResponse<RegistrationResponse>> getPaged(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.getPaged(userId, status, page, size, sortBy, direction));
    }

    @GetMapping("/aggregates")
    public ResponseEntity<RegistrationAggregateResponse> getAggregates() {
        return ResponseEntity.ok(service.getAggregates());
    }
}
