package com.courseflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.courseflow.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByDepartmentContainingIgnoreCase(String department, Pageable pageable);

    Page<Course> findByCourseNameContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String courseName,
                                                                                       String courseCode,
                                                                                       Pageable pageable);

    @Query("select coalesce(sum(c.capacity), 0) from Course c")
    long sumCapacity();
}
