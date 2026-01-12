package com.studentpipeline.service;

import com.studentpipeline.dto.ProgressResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking progress of long-running operations
 */
@Service
public class ProgressTracker {
    
    private final Map<String, ProgressInfo> progressMap = new ConcurrentHashMap<>();
    
    public void startProgress(String operationId, long totalRecords) {
        ProgressInfo info = new ProgressInfo();
        info.operationId = operationId;
        info.totalRecords = totalRecords;
        info.currentRecords = 0;
        info.startTime = System.currentTimeMillis();
        info.completed = false;
        info.status = "IN_PROGRESS";
        info.message = "Starting operation...";
        progressMap.put(operationId, info);
    }
    
    public void updateProgress(String operationId, long currentRecords, String message) {
        ProgressInfo info = progressMap.get(operationId);
        if (info != null) {
            info.currentRecords = currentRecords;
            if (message != null) {
                info.message = message;
            }
        }
    }
    
    public void completeProgress(String operationId, String message) {
        ProgressInfo info = progressMap.get(operationId);
        if (info != null) {
            info.completed = true;
            info.status = "COMPLETED";
            info.message = message;
            info.currentRecords = info.totalRecords;
        }
    }
    
    public void failProgress(String operationId, String message) {
        ProgressInfo info = progressMap.get(operationId);
        if (info != null) {
            info.completed = true;
            info.status = "FAILED";
            info.message = message;
        }
    }
    
    public ProgressResponse getProgress(String operationId) {
        ProgressInfo info = progressMap.get(operationId);
        if (info == null) {
            return null;
        }
        
        long elapsedTime = System.currentTimeMillis() - info.startTime;
        return new ProgressResponse(
            info.operationId,
            info.currentRecords,
            info.totalRecords,
            elapsedTime,
            info.completed,
            info.status,
            info.message
        );
    }
    
    public void removeProgress(String operationId) {
        progressMap.remove(operationId);
    }
    
    private static class ProgressInfo {
        String operationId;
        long totalRecords;
        long currentRecords;
        long startTime;
        boolean completed;
        String status;
        String message;
    }
}
