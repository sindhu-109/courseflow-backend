package com.courseflow.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.courseflow.dto.CourseResponse;
import com.courseflow.dto.PagedResponse;
import com.courseflow.dto.RegistrationDecisionRequest;
import com.courseflow.dto.RegistrationAggregateResponse;
import com.courseflow.dto.RegistrationResponse;
import com.courseflow.dto.UserResponse;
import com.courseflow.model.Course;
import com.courseflow.model.Registration;
import com.courseflow.model.User;
import com.courseflow.repository.CourseRepository;
import com.courseflow.repository.RegistrationRepository;
import com.courseflow.repository.UserRepository;

@Service
public class RegistrationService {

    private final RegistrationRepository regRepo;
    private final UserRepository userRepo;
    private final CourseRepository courseRepo;

    public RegistrationService(RegistrationRepository regRepo, UserRepository userRepo, CourseRepository courseRepo) {
        this.regRepo = regRepo;
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
    }

    public RegistrationResponse register(Long userId, Long courseId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("Blocked".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("Blocked users cannot register");
        }

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyRegistered = regRepo.existsByUser_IdAndCourse_IdAndStatusIn(
                userId,
                courseId,
                Set.of("Pending", "Approved"));
        if (alreadyRegistered) {
            throw new RuntimeException("Registration already exists for this user and course");
        }

        long approvedCount = regRepo.countByCourse_IdAndStatus(courseId, "Approved");
        if (approvedCount >= course.getCapacity()) {
            throw new RuntimeException("Course capacity exceeded");
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setCourse(course);
        registration.setStatus("Pending");
        registration.setCreatedAt(LocalDateTime.now());
        registration.setDecisionNote("");
        registration.setRejectionReason("");

        return toResponse(regRepo.save(registration));
    }

    public List<RegistrationResponse> getAll() {
        return regRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RegistrationResponse> getPending() {
        return regRepo.findByStatus("Pending").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<RegistrationResponse> getPaged(Long userId,
                                                        String status,
                                                        int page,
                                                        int size,
                                                        String sortBy,
                                                        String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseDirection(direction), normalizeRegistrationSort(sortBy)));

        Page<Registration> registrationPage;
        if (userId != null) {
            registrationPage = regRepo.findByUser_Id(userId, pageable);
        } else if (status != null && !status.isBlank()) {
            registrationPage = regRepo.findByStatus(status, pageable);
        } else {
            registrationPage = regRepo.findAll(pageable);
        }

        PagedResponse<RegistrationResponse> response = new PagedResponse<>();
        response.setContent(registrationPage.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setPage(registrationPage.getNumber());
        response.setSize(registrationPage.getSize());
        response.setTotalElements(registrationPage.getTotalElements());
        response.setTotalPages(registrationPage.getTotalPages());
        response.setFirst(registrationPage.isFirst());
        response.setLast(registrationPage.isLast());
        response.setSortBy(normalizeRegistrationSort(sortBy));
        response.setDirection(direction == null || direction.isBlank() ? "desc" : direction.toLowerCase());
        return response;
    }

    public RegistrationAggregateResponse getAggregates() {
        RegistrationAggregateResponse response = new RegistrationAggregateResponse();
        response.setTotalRegistrations(regRepo.count());
        response.setApprovedCount(regRepo.countByStatusIgnoreCase("Approved"));
        response.setPendingCount(regRepo.countByStatusIgnoreCase("Pending"));
        response.setRejectedCount(regRepo.countByStatusIgnoreCase("Rejected"));
        return response;
    }

    public RegistrationResponse approve(Long id, RegistrationDecisionRequest request) {
        Registration registration = regRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        long approvedCount = regRepo.countByCourse_IdAndStatus(registration.getCourse().getId(), "Approved");
        if (!"Approved".equalsIgnoreCase(registration.getStatus())
                && approvedCount >= registration.getCourse().getCapacity()) {
            throw new RuntimeException("Course capacity exceeded");
        }

        registration.setStatus("Approved");
        registration.setDecisionAt(LocalDateTime.now());
        registration.setDecisionNote(request != null && request.getDecisionNote() != null
                ? request.getDecisionNote()
                : "");
        registration.setRejectionReason("");

        return toResponse(regRepo.save(registration));
    }

    public RegistrationResponse reject(Long id, RegistrationDecisionRequest request) {
        Registration registration = regRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        registration.setStatus("Rejected");
        registration.setDecisionAt(LocalDateTime.now());
        registration.setDecisionNote(request != null && request.getDecisionNote() != null
                ? request.getDecisionNote()
                : "");
        registration.setRejectionReason(request != null && request.getRejectionReason() != null
                ? request.getRejectionReason()
                : "Rejected");

        return toResponse(regRepo.save(registration));
    }

    private RegistrationResponse toResponse(Registration registration) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setStatus(registration.getStatus());
        response.setCreatedAt(registration.getCreatedAt());
        response.setUpdatedAt(registration.getUpdatedAt());
        response.setDecisionAt(registration.getDecisionAt());
        response.setDecisionNote(registration.getDecisionNote());
        response.setRejectionReason(registration.getRejectionReason());
        response.setUser(toUserResponse(registration.getUser()));
        response.setCourse(toCourseResponse(registration.getCourse()));
        return response;
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setJoinedAt(user.getJoinedAt());
        response.setLastActiveAt(user.getLastActiveAt());
        return response;
    }

    private CourseResponse toCourseResponse(Course course) {
        long enrolledCount = regRepo.countByCourse_IdAndStatus(course.getId(), "Approved");

        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setCourseName(course.getCourseName());
        response.setCourseCode(course.getCourseCode());
        response.setFaculty(course.getFaculty());
        response.setFacultyProfile(course.getFacultyProfile());
        response.setTime(course.getTime());
        response.setCredits(course.getCredits());
        response.setCapacity(course.getCapacity());
        response.setDepartment(course.getDepartment());
        response.setSemester(course.getSemester());
        response.setMode(course.getMode());
        response.setRoom(course.getRoom());
        response.setPrerequisites(course.getPrerequisites());
        response.setDescription(course.getDescription());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        response.setEnrolledCount(enrolledCount);
        response.setAvailableSeats(Math.max(course.getCapacity() - enrolledCount, 0));
        return response;
    }

    private Sort.Direction parseDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String normalizeRegistrationSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "createdAt";
        }
        return switch (sortBy) {
            case "createdAt", "updatedAt", "decisionAt", "status" -> sortBy;
            default -> "createdAt";
        };
    }
}
