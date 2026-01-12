package com.studentpipeline.service;

import com.studentpipeline.dto.StudentDto;
import com.studentpipeline.entity.Student;
import com.studentpipeline.repository.StudentRepository;
import com.studentpipeline.util.StudentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentReportService {

    private static final Logger logger = LoggerFactory.getLogger(StudentReportService.class);

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Get paginated students with optional filters
     */
    public Page<StudentDto> getStudents(int page, int size, Long studentId, String className) {
        logger.info("Fetching students - page: {}, size: {}, studentId: {}, className: {}", 
                   page, size, studentId, className);

        // Create pageable with sorting by student ID
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentId").ascending());

        // Fetch students with filters
        Page<Student> studentPage = studentRepository.findStudentsWithFilters(studentId, className, pageable);

        // Convert to DTOs
        Page<StudentDto> studentDtoPage = studentPage.map(StudentMapper::toDto);

        logger.info("Found {} students (total elements: {}, total pages: {})", 
                   studentDtoPage.getNumberOfElements(),
                   studentDtoPage.getTotalElements(),
                   studentDtoPage.getTotalPages());

        return studentDtoPage;
    }

    /**
     * Get all students with filters (for exports)
     */
    public List<StudentDto> getAllStudentsWithFilters(Long studentId, String className) {
        logger.info("Fetching all students for export - studentId: {}, className: {}", studentId, className);

        List<Student> students = studentRepository.findAllStudentsWithFilters(studentId, className);
        List<StudentDto> studentDtos = students.stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());

        logger.info("Found {} students for export", studentDtos.size());
        return studentDtos;
    }

    /**
     * Get student by student ID
     */
    public StudentDto getStudentByStudentId(Long studentId) {
        logger.info("Fetching student by student ID: {}", studentId);

        return studentRepository.findByStudentId(studentId)
                .map(StudentMapper::toDto)
                .orElse(null);
    }

    /**
     * Get students count by class
     */
    public long getStudentCountByClass(String className) {
        logger.info("Getting student count for class: {}", className);
        return studentRepository.countByClassName(className);
    }

    /**
     * Get total students count
     */
    public long getTotalStudentsCount() {
        logger.info("Getting total students count");
        return studentRepository.count();
    }

    /**
     * Check if a student exists by student ID
     */
    public boolean existsByStudentId(Long studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    /**
     * Delete student by database ID
     */
    public boolean deleteStudentById(Long id) {
        logger.info("Deleting student by ID: {}", id);
        
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            logger.info("Successfully deleted student with ID: {}", id);
            return true;
        }
        
        logger.warn("Student with ID {} not found for deletion", id);
        return false;
    }
}