package com.courseflow.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.courseflow.model.Registration;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByUser_Id(Long userId);

    List<Registration> findByStatus(String status);

    Page<Registration> findByStatus(String status, Pageable pageable);

    Page<Registration> findByUser_Id(Long userId, Pageable pageable);

    boolean existsByUser_IdAndCourse_IdAndStatusIn(Long userId, Long courseId, Collection<String> statuses);

    long countByCourse_IdAndStatus(Long courseId, String status);

    @Query("select count(r) from Registration r where lower(r.status) = lower(:status)")
    long countByStatusIgnoreCase(String status);
}
