package com.studentpipeline.dto;

/**
 * DTO for progress tracking
 */
public class ProgressResponse {
    private String operationId;
    private long currentRecords;
    private long totalRecords;
    private long elapsedTimeMs;
    private boolean completed;
    private String status;
    private String message;

    public ProgressResponse() {}

    public ProgressResponse(String operationId, long currentRecords, long totalRecords, 
                           long elapsedTimeMs, boolean completed, String status, String message) {
        this.operationId = operationId;
        this.currentRecords = currentRecords;
        this.totalRecords = totalRecords;
        this.elapsedTimeMs = elapsedTimeMs;
        this.completed = completed;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public long getCurrentRecords() {
        return currentRecords;
    }

    public void setCurrentRecords(long currentRecords) {
        this.currentRecords = currentRecords;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(long elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
