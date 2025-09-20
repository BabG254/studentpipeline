package com.studentpipeline.controller;

import com.opencsv.exceptions.CsvException;
import com.studentpipeline.dto.ApiResponse;
import com.studentpipeline.dto.FileOperationResponse;
import com.studentpipeline.dto.GenerateExcelRequest;
import com.studentpipeline.service.CsvToDatabaseService;
import com.studentpipeline.service.ExcelGenerationService;
import com.studentpipeline.service.ExcelToCsvService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessingController.class);

    @Autowired
    private ExcelGenerationService excelGenerationService;

    @Autowired
    private ExcelToCsvService excelToCsvService;

    @Autowired
    private CsvToDatabaseService csvToDatabaseService;

    /**
     * Generate Excel file with specified number of student records
     * POST /api/generate-excel
     */
    @PostMapping("/generate-excel")
    public ResponseEntity<ApiResponse<FileOperationResponse>> generateExcel(
            @Valid @RequestBody GenerateExcelRequest request) {
        
        logger.info("Received request to generate Excel with {} records, fileName: {}", 
                   request.getRecords(), request.getFileName());

        try {
            FileOperationResponse response = excelGenerationService.generateExcel(
                    request.getRecords(), request.getFileName());
            
            logger.info("Excel generation completed successfully: {}", response.getPath());
            return ResponseEntity.ok(ApiResponse.success("Excel file generated successfully", response));
            
        } catch (IOException e) {
            logger.error("Error generating Excel file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate Excel file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during Excel generation: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Convert Excel file to CSV with score adjustment (+10)
     * POST /api/convert-excel-to-csv
     */
    @PostMapping("/convert-excel-to-csv")
    public ResponseEntity<ApiResponse<FileOperationResponse>> convertExcelToCsv(
            @RequestParam("file") MultipartFile file) {
        
        logger.info("Received request to convert Excel to CSV, file: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        if (!isExcelFile(file)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be an Excel file (.xlsx or .xls)"));
        }

        try {
            FileOperationResponse response = excelToCsvService.convertExcelToCsv(file);
            
            logger.info("Excel to CSV conversion completed successfully: {}", response.getPath());
            return ResponseEntity.ok(ApiResponse.success("Excel converted to CSV successfully", response));
            
        } catch (IOException e) {
            logger.error("Error converting Excel to CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to convert Excel to CSV: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during Excel to CSV conversion: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Upload CSV file and save to database
     * POST /api/upload-csv-to-db
     */
    @PostMapping("/upload-csv-to-db")
    public ResponseEntity<ApiResponse<FileOperationResponse>> uploadCsvToDatabase(
            @RequestParam("file") MultipartFile file) {
        
        logger.info("Received request to upload CSV to database, file: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        if (!isCsvFile(file)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File must be a CSV file (.csv)"));
        }

        try {
            FileOperationResponse response = csvToDatabaseService.uploadCsvToDatabase(file);
            
            logger.info("CSV to database upload completed successfully. Records processed: {}", 
                       response.getRecordsProcessed());
            return ResponseEntity.ok(ApiResponse.success("CSV uploaded to database successfully", response));
            
        } catch (IOException e) {
            logger.error("Error uploading CSV to database: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to upload CSV to database: " + e.getMessage()));
        } catch (CsvException e) {
            logger.error("CSV parsing error: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("CSV format error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during CSV upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Service is running"));
    }

    // Helper methods

    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && (
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel")
        )) || (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")));
    }

    private boolean isCsvFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && (
                contentType.equals("text/csv") ||
                contentType.equals("application/csv") ||
                contentType.equals("text/plain")
        )) || (fileName != null && fileName.endsWith(".csv"));
    }
}