package com.studentpipeline.dto;

/**
 * Response DTO for file operations
 */
public class FileOperationResponse {
    
    private String path;
    private String fileName;
    private Long recordsProcessed;
    private String operation;

    // Constructors
    public FileOperationResponse() {}

    public FileOperationResponse(String path, String fileName, Long recordsProcessed, String operation) {
        this.path = path;
        this.fileName = fileName;
        this.recordsProcessed = recordsProcessed;
        this.operation = operation;
    }

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "FileOperationResponse{" +
                "path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", recordsProcessed=" + recordsProcessed +
                ", operation='" + operation + '\'' +
                '}';
    }
}