package com.courseflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.courseflow.dto.CourseAggregateResponse;
import com.courseflow.dto.CourseResponse;
import com.courseflow.dto.PagedResponse;
import com.courseflow.model.Course;
import com.courseflow.repository.CourseRepository;
import com.courseflow.repository.RegistrationRepository;

@Service
public class CourseService {

    private final CourseRepository repo;
    private final RegistrationRepository registrationRepository;

    public CourseService(CourseRepository repo, RegistrationRepository registrationRepository) {
        this.repo = repo;
        this.registrationRepository = registrationRepository;
    }

    public List<CourseResponse> getAll() {
        return repo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse add(Course course) {
        return toResponse(repo.save(course));
    }

    public CourseResponse update(Long id, Course course) {
        Course existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        existing.setCourseName(course.getCourseName());
        existing.setCourseCode(course.getCourseCode());
        existing.setFaculty(course.getFaculty());
        existing.setFacultyProfile(course.getFacultyProfile());
        existing.setTime(course.getTime());
        existing.setCredits(course.getCredits());
        existing.setCapacity(course.getCapacity());
        existing.setDepartment(course.getDepartment());
        existing.setSemester(course.getSemester());
        existing.setMode(course.getMode());
        existing.setRoom(course.getRoom());
        existing.setPrerequisites(course.getPrerequisites());
        existing.setDescription(course.getDescription());

        return toResponse(repo.save(existing));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public PagedResponse<CourseResponse> getPaged(String department,
                                                  String search,
                                                  int page,
                                                  int size,
                                                  String sortBy,
                                                  String direction) {
        Sort sort = Sort.by(parseDirection(direction), normalizeCourseSort(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Course> coursePage;
        if (department != null && !department.isBlank()) {
            coursePage = repo.findByDepartmentContainingIgnoreCase(department.trim(), pageable);
        } else if (search != null && !search.isBlank()) {
            String keyword = search.trim();
            coursePage = repo.findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(
                    keyword,
                    keyword,
                    pageable);
        } else {
            coursePage = repo.findAll(pageable);
        }

        return toPagedResponse(coursePage, sortBy, direction);
    }

    public CourseAggregateResponse getAggregates() {
        List<CourseResponse> courses = getAll();
        long totalApprovedRegistrations = courses.stream()
                .mapToLong(CourseResponse::getEnrolledCount)
                .sum();

        CourseAggregateResponse response = new CourseAggregateResponse();
        response.setTotalCourses(repo.count());
        response.setTotalCapacity(repo.sumCapacity());
        response.setTotalApprovedRegistrations(totalApprovedRegistrations);
        response.setTotalAvailableSeats(Math.max(response.getTotalCapacity() - totalApprovedRegistrations, 0));
        return response;
    }

    private PagedResponse<CourseResponse> toPagedResponse(Page<Course> coursePage, String sortBy, String direction) {
        PagedResponse<CourseResponse> response = new PagedResponse<>();
        response.setContent(coursePage.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setPage(coursePage.getNumber());
        response.setSize(coursePage.getSize());
        response.setTotalElements(coursePage.getTotalElements());
        response.setTotalPages(coursePage.getTotalPages());
        response.setFirst(coursePage.isFirst());
        response.setLast(coursePage.isLast());
        response.setSortBy(normalizeCourseSort(sortBy));
        response.setDirection(direction == null || direction.isBlank() ? "asc" : direction.toLowerCase());
        return response;
    }

    private Sort.Direction parseDirection(String direction) {
        return "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String normalizeCourseSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "courseName";
        }
        return switch (sortBy) {
            case "courseCode", "department", "semester", "capacity", "credits", "createdAt", "updatedAt", "courseName" -> sortBy;
            default -> "courseName";
        };
    }

    private CourseResponse toResponse(Course course) {
        long enrolledCount = registrationRepository.countByCourse_IdAndStatus(course.getId(), "Approved");

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
}
