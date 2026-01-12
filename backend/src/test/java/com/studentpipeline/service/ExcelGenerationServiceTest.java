package com.studentpipeline.service;

import com.studentpipeline.config.DataPathConfig;
import com.studentpipeline.dto.FileOperationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelGenerationServiceTest {

    @Mock
    private DataPathConfig dataPathConfig;

    @InjectMocks
    private ExcelGenerationService excelGenerationService;

    private String testDataPath;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("test-excel-generation");
        testDataPath = tempDir.toString();
        
        when(dataPathConfig.getBase()).thenReturn(testDataPath);
    }

    @Test
    void testGenerateExcel_SmallDataset() throws IOException {
        // Given
        long recordCount = 100;
        String fileName = "test-students.xlsx";
        String operationId = "test-op-1";

        // When
        FileOperationResponse response = excelGenerationService.generateExcel(recordCount, fileName, operationId);

        // Then
        assertNotNull(response);
        assertEquals(fileName, response.getFileName());
        assertEquals(recordCount, response.getRecordsProcessed());
        assertEquals("EXCEL_GENERATION", response.getOperation());
        
        // Verify file exists
        Path filePath = Paths.get(response.getPath());
        assertTrue(Files.exists(filePath));
        assertTrue(Files.size(filePath) > 0);
        
        // Clean up
        Files.deleteIfExists(filePath);
    }

    @Test
    void testGenerateExcel_DefaultFileName() throws IOException {
        // Given
        long recordCount = 10;
        String fileName = null;
        String operationId = "test-op-2";

        // When
        FileOperationResponse response = excelGenerationService.generateExcel(recordCount, fileName, operationId);

        // Then
        assertNotNull(response);
        assertTrue(response.getFileName().contains("students-"));
        assertTrue(response.getFileName().endsWith(".xlsx"));
        assertEquals(recordCount, response.getRecordsProcessed());
        
        // Clean up
        Files.deleteIfExists(Paths.get(response.getPath()));
    }

    @Test
    void testGenerateExcel_LargeDataset() throws IOException {
        // Given
        long recordCount = 1000; // Use a reasonable size for unit tests
        String fileName = "large-test.xlsx";
        String operationId = "test-op-3";

        // When
        long startTime = System.currentTimeMillis();
        FileOperationResponse response = excelGenerationService.generateExcel(recordCount, fileName, operationId);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(response);
        assertEquals(recordCount, response.getRecordsProcessed());
        
        // Verify performance (should complete within reasonable time)
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 30000, "Generation took too long: " + executionTime + "ms");
        
        // Verify file size is reasonable
        Path filePath = Paths.get(response.getPath());
        long fileSize = Files.size(filePath);
        assertTrue(fileSize > recordCount * 50, "File size seems too small"); // Rough estimate
        
        // Clean up
        Files.deleteIfExists(filePath);
    }
}