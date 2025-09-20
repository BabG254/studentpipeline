package com.studentpipeline.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student", indexes = {
    @Index(name = "idx_student_student_id", columnList = "student_id"),
    @Index(name = "idx_student_class_name", columnList = "class_name"),
    @Index(name = "idx_student_score", columnList = "score")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    @NotNull(message = "Student ID cannot be null")
    private Long studentId;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Column(name = "dob", nullable = false)
    @NotNull(message = "Date of birth cannot be null")
    private LocalDate dob;

    @Column(name = "class_name", nullable = false, length = 20)
    @NotBlank(message = "Class name cannot be blank")
    @Size(max = 20, message = "Class name cannot exceed 20 characters")
    private String className;

    @Column(name = "score", nullable = false)
    @NotNull(message = "Score cannot be null")
    private Integer score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Student() {
        this.createdAt = LocalDateTime.now();
    }

    public Student(Long studentId, String firstName, String lastName, 
                   LocalDate dob, String className, Integer score) {
        this();
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.className = className;
        this.score = score;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                ", className='" + className + '\'' +
                ", score=" + score +
                ", createdAt=" + createdAt +
                '}';
    }
}