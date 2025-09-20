package com.studentpipeline.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for Excel generation
 */
public class GenerateExcelRequest {
    
    @NotNull(message = "Records count cannot be null")
    @Min(value = 1, message = "Records count must be at least 1")
    private Long records;
    
    private String fileName;

    // Constructors
    public GenerateExcelRequest() {}

    public GenerateExcelRequest(Long records, String fileName) {
        this.records = records;
        this.fileName = fileName;
    }

    // Getters and Setters
    public Long getRecords() {
        return records;
    }

    public void setRecords(Long records) {
        this.records = records;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "GenerateExcelRequest{" +
                "records=" + records +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}