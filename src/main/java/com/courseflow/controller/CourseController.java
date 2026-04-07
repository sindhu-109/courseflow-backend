package com.courseflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.courseflow.dto.CourseAggregateResponse;
import com.courseflow.dto.CourseResponse;
import com.courseflow.dto.PagedResponse;
import com.courseflow.model.Course;
import com.courseflow.service.CourseService;

@RestController
@RequestMapping("/courses")
@CrossOrigin
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<PagedResponse<CourseResponse>> getPaged(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "courseName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(service.getPaged(department, search, page, size, sortBy, direction));
    }

    @GetMapping("/aggregates")
    public ResponseEntity<CourseAggregateResponse> getAggregates() {
        return ResponseEntity.ok(service.getAggregates());
    }

    @PostMapping
    public ResponseEntity<CourseResponse> add(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id, @RequestBody Course course) {
        return ResponseEntity.ok(service.update(id, course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
