package com.studentpipeline.controller;

import com.studentpipeline.dto.ApiResponse;
import com.studentpipeline.dto.StudentDto;
import com.studentpipeline.service.ExportService;
import com.studentpipeline.service.StudentReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentReportController {

    private static final Logger logger = LoggerFactory.getLogger(StudentReportController.class);

    @Autowired
    private StudentReportService studentReportService;

    @Autowired
    private ExportService exportService;

    /**
     * Get paginated students with optional filters
     * GET /api/students
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Page<StudentDto>>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className) {

        logger.info("Fetching students - page: {}, size: {}, studentId: {}, className: {}", 
                   page, size, studentId, className);

        try {
            // Validate parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page number cannot be negative"));
            }
            
            if (size <= 0 || size > 1000) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Page size must be between 1 and 1000"));
            }

            Page<StudentDto> students = studentReportService.getStudents(page, size, studentId, className);
            
            logger.info("Found {} students (total: {})", students.getNumberOfElements(), students.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", students));
            
        } catch (Exception e) {
            logger.error("Error fetching students: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    /**
     * Get student by student ID
     * GET /api/students/{studentId}
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentByStudentId(@PathVariable Long studentId) {
        logger.info("Fetching student by student ID: {}", studentId);

        try {
            StudentDto student = studentReportService.getStudentByStudentId(studentId);
            
            if (student == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(ApiResponse.success("Student found", student));
            
        } catch (Exception e) {
            logger.error("Error fetching student by ID {}: {}", studentId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch student: " + e.getMessage()));
        }
    }

    /**
     * Export students to Excel, CSV, or PDF
     * GET /api/students/export
     */
    @GetMapping("/students/export")
    public ResponseEntity<?> exportStudents(
            @RequestParam String format,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String fileName) {

        logger.info("Exporting students - format: {}, studentId: {}, className: {}, fileName: {}", 
                   format, studentId, className, fileName);

        try {
            // Validate format
            if (!isValidExportFormat(format)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid export format. Supported formats: excel, csv, pdf"));
            }

            // Get students with filters
            List<StudentDto> students = studentReportService.getAllStudentsWithFilters(studentId, className);
            
            if (students.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("No students found matching the criteria", null));
            }

            // Export based on format
            switch (format.toLowerCase()) {
                case "excel":
                    return exportService.exportToExcel(students, fileName);
                case "csv":
                    return exportService.exportToCsv(students, fileName);
                case "pdf":
                    return exportService.exportToPdf(students, fileName);
                default:
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Unsupported export format: " + format));
            }
            
        } catch (IOException e) {
            logger.error("Error exporting students to {}: {}", format, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to export students: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during export: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get statistics about students
     * GET /api/students/stats
     */
    @GetMapping("/students/stats")
    public ResponseEntity<ApiResponse<Object>> getStudentStats() {
        logger.info("Fetching student statistics");

        try {
            long totalStudents = studentReportService.getTotalStudentsCount();
            
            // Get count by class
            long class1Count = studentReportService.getStudentCountByClass("Class1");
            long class2Count = studentReportService.getStudentCountByClass("Class2");
            long class3Count = studentReportService.getStudentCountByClass("Class3");
            long class4Count = studentReportService.getStudentCountByClass("Class4");
            long class5Count = studentReportService.getStudentCountByClass("Class5");

            Object stats = new Object() {
                public final long total = totalStudents;
                public final Object byClass = new Object() {
                    public final long Class1 = class1Count;
                    public final long Class2 = class2Count;
                    public final long Class3 = class3Count;
                    public final long Class4 = class4Count;
                    public final long Class5 = class5Count;
                };
            };
            
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
            
        } catch (Exception e) {
            logger.error("Error fetching student statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch statistics: " + e.getMessage()));
        }
    }

    /**
     * Delete student by ID
     * DELETE /api/students/{id}
     */
    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteStudent(@PathVariable Long id) {
        logger.info("Deleting student by ID: {}", id);

        try {
            boolean deleted = studentReportService.deleteStudentById(id);
            
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Student deleted successfully", null));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Student not found with ID: " + id));
            }
            
        } catch (Exception e) {
            logger.error("Error deleting student {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to delete student: " + e.getMessage()));
        }
    }

    // Helper methods

    private boolean isValidExportFormat(String format) {
        if (format == null) return false;
        String lowerFormat = format.toLowerCase();
        return lowerFormat.equals("excel") || lowerFormat.equals("csv") || lowerFormat.equals("pdf");
    }
}