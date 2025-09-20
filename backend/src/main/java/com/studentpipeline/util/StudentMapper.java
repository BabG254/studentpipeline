package com.studentpipeline.util;

import com.studentpipeline.dto.StudentDto;
import com.studentpipeline.entity.Student;
import com.studentpipeline.model.StudentRow;

/**
 * Mapper utility class for converting between different student representations
 */
public class StudentMapper {

    private StudentMapper() {
        // Utility class - private constructor
    }

    /**
     * Convert Student entity to StudentDto
     */
    public static StudentDto toDto(Student student) {
        if (student == null) {
            return null;
        }
        
        return new StudentDto(
            student.getId(),
            student.getStudentId(),
            student.getFirstName(),
            student.getLastName(),
            student.getDob(),
            student.getClassName(),
            student.getScore(),
            student.getCreatedAt()
        );
    }

    /**
     * Convert StudentDto to Student entity
     */
    public static Student toEntity(StudentDto dto) {
        if (dto == null) {
            return null;
        }
        
        Student student = new Student(
            dto.getStudentId(),
            dto.getFirstName(),
            dto.getLastName(),
            dto.getDob(),
            dto.getClassName(),
            dto.getScore()
        );
        
        student.setId(dto.getId());
        if (dto.getCreatedAt() != null) {
            student.setCreatedAt(dto.getCreatedAt());
        }
        
        return student;
    }

    /**
     * Convert StudentRow to Student entity
     */
    public static Student rowToEntity(StudentRow row) {
        if (row == null) {
            return null;
        }
        
        return new Student(
            row.getStudentId(),
            row.getFirstName(),
            row.getLastName(),
            row.getDob(),
            row.getClassName(),
            row.getScore()
        );
    }

    /**
     * Convert Student entity to StudentRow
     */
    public static StudentRow entityToRow(Student student) {
        if (student == null) {
            return null;
        }
        
        return new StudentRow(
            student.getStudentId(),
            student.getFirstName(),
            student.getLastName(),
            student.getDob(),
            student.getClassName(),
            student.getScore()
        );
    }

    /**
     * Convert StudentRow to StudentDto
     */
    public static StudentDto rowToDto(StudentRow row) {
        if (row == null) {
            return null;
        }
        
        return new StudentDto(
            null, // ID is not available in row
            row.getStudentId(),
            row.getFirstName(),
            row.getLastName(),
            row.getDob(),
            row.getClassName(),
            row.getScore(),
            null // CreatedAt is not available in row
        );
    }

    /**
     * Convert StudentDto to StudentRow
     */
    public static StudentRow dtoToRow(StudentDto dto) {
        if (dto == null) {
            return null;
        }
        
        return new StudentRow(
            dto.getStudentId(),
            dto.getFirstName(),
            dto.getLastName(),
            dto.getDob(),
            dto.getClassName(),
            dto.getScore()
        );
    }
}