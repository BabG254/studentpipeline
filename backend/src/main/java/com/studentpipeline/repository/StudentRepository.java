package com.studentpipeline.repository;

import com.studentpipeline.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by student ID
     */
    Optional<Student> findByStudentId(Long studentId);

    /**
     * Find students by class name with pagination
     */
    Page<Student> findByClassName(String className, Pageable pageable);

    /**
     * Find students by student ID and class name with pagination
     */
    Page<Student> findByStudentIdAndClassName(Long studentId, String className, Pageable pageable);

    /**
     * Check if student with given student ID exists
     */
    boolean existsByStudentId(Long studentId);

    /**
     * Custom query to find students with filters
     */
    @Query("SELECT s FROM Student s WHERE " +
           "(:studentId IS NULL OR s.studentId = :studentId) AND " +
           "(:className IS NULL OR s.className = :className)")
    Page<Student> findStudentsWithFilters(@Param("studentId") Long studentId,
                                        @Param("className") String className,
                                        Pageable pageable);

    /**
     * Get all students with filters (for exports)
     */
    @Query("SELECT s FROM Student s WHERE " +
           "(:studentId IS NULL OR s.studentId = :studentId) AND " +
           "(:className IS NULL OR s.className = :className) " +
           "ORDER BY s.studentId ASC")
    List<Student> findAllStudentsWithFilters(@Param("studentId") Long studentId,
                                           @Param("className") String className);

    /**
     * Count students by class name
     */
    long countByClassName(String className);
}