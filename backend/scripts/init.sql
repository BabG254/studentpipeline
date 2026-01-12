-- Database initialization script for Student Pipeline
CREATE DATABASE IF NOT EXISTS studentpipeline;

-- Create the student table
CREATE TABLE IF NOT EXISTS student (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    class_name VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_student_student_id ON student(student_id);
CREATE INDEX IF NOT EXISTS idx_student_class_name ON student(class_name);
CREATE INDEX IF NOT EXISTS idx_student_score ON student(score);

-- Create a script data directory path setup
-- This will be handled by the application configuration