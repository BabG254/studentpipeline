package com.studentpipeline.service;

import com.studentpipeline.config.DataPathConfig;
import com.studentpipeline.dto.FileOperationResponse;
import com.studentpipeline.model.StudentRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class ExcelGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelGenerationService.class);
    
    // Static block to configure POI for large files
    static {
        // Allow larger Excel files (500MB limit for 1M+ records)
        IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024);
    }
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Jessica", 
        "Robert", "Ashley", "William", "Amanda", "Christopher", "Jennifer", "Matthew",
        "Lisa", "Anthony", "Michelle", "Mark", "Kimberly", "Donald", "Amy", "Steven",
        "Angela", "Andrew", "Helen", "Kenneth", "Deborah", "Paul", "Dorothy"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
        "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };
    
    private static final String[] CLASS_NAMES = {
        "Class1", "Class2", "Class3", "Class4", "Class5"
    };
    
    private static final int SCORE_MIN = 55;
    private static final int SCORE_MAX = 75;
    private static final int PROGRESS_LOG_INTERVAL = 50000;
    private static final int PROGRESS_UPDATE_INTERVAL = 1000; // Update progress every 1000 records
    
    @Autowired
    private DataPathConfig dataPathConfig;
    
    @Autowired
    private ProgressTracker progressTracker;
    
    private final Random random = new Random();

    /**
     * Generate Excel file with specified number of student records
     */
    public FileOperationResponse generateExcel(long recordCount, String fileName, String operationId) throws IOException {
        logger.info("Starting Excel generation for {} records", recordCount);
        
        // Ensure data directory exists
        Path dataDir = Paths.get(dataPathConfig.getBase());
        Files.createDirectories(dataDir);
        
        // Generate file name if not provided
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "students-" + recordCount + "-" + System.currentTimeMillis() + ".xlsx";
        } else {
            // Ensure unique filename by adding timestamp if file exists
            if (!fileName.endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            Path checkPath = dataDir.resolve(fileName);
            if (Files.exists(checkPath)) {
                String baseName = fileName.substring(0, fileName.length() - 5); // Remove .xlsx
                fileName = baseName + "-" + System.currentTimeMillis() + ".xlsx";
            }
        }
        
        Path filePath = dataDir.resolve(fileName);
        logger.info("Generating Excel file: {}", filePath.toAbsolutePath());
        
        // Initialize progress tracking
        if (operationId != null) {
            progressTracker.startProgress(operationId, recordCount);
        }
        
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(1000); // Keep 1000 rows in memory
             FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
            
            SXSSFSheet sheet = workbook.createSheet("Students");
            
            // Create date format for DOB column
            CellStyle dateStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            dateStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd"));
            
            // Create header row
            createHeaderRow(sheet);
            
            // Generate data rows
            long startTime = System.currentTimeMillis();
            for (long i = 1; i <= recordCount; i++) {
                StudentRow studentRow = generateRandomStudentRow(i);
                createDataRow(sheet, (int) i, studentRow, dateStyle);
                
                // Update progress tracker
                if (operationId != null && i % PROGRESS_UPDATE_INTERVAL == 0) {
                    String message = String.format("Generated %,d of %,d records", i, recordCount);
                    progressTracker.updateProgress(operationId, i, message);
                }
                
                // Log progress
                if (i % PROGRESS_LOG_INTERVAL == 0) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    logger.info("Generated {} rows in {} ms", i, elapsed);
                }
                
                // Flush rows to disk periodically to save memory
                if (i % 1000 == 0) {
                    ((SXSSFSheet) sheet).flushRows(100);
                }
            }
            
            // Write to file
            workbook.write(fileOut);
            workbook.dispose(); // Clean up temporary files
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("Excel generation completed. {} records written to {} in {} ms", 
                       recordCount, filePath.getFileName(), totalTime);
            
            // Mark progress as complete
            if (operationId != null) {
                String message = String.format("Completed: %,d records generated in %,d ms", recordCount, totalTime);
                progressTracker.completeProgress(operationId, message);
            }
            
            return new FileOperationResponse(
                filePath.toAbsolutePath().toString(),
                fileName,
                recordCount,
                "EXCEL_GENERATION"
            );
        } catch (Exception e) {
            // Mark progress as failed
            if (operationId != null) {
                progressTracker.failProgress(operationId, "Generation failed: " + e.getMessage());
            }
            throw e;
        }
    }
    
    private void createHeaderRow(SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        Cell cell0 = headerRow.createCell(0);
        cell0.setCellValue("studentId");
        
        Cell cell1 = headerRow.createCell(1);
        cell1.setCellValue("firstName");
        
        Cell cell2 = headerRow.createCell(2);
        cell2.setCellValue("lastName");
        
        Cell cell3 = headerRow.createCell(3);
        cell3.setCellValue("DOB");
        
        Cell cell4 = headerRow.createCell(4);
        cell4.setCellValue("class");
        
        Cell cell5 = headerRow.createCell(5);
        cell5.setCellValue("score");
        
        // Auto-size columns for headers
        for (int i = 0; i < 6; i++) {
            sheet.trackColumnForAutoSizing(i);
        }
    }
    
    private void createDataRow(SXSSFSheet sheet, int rowNum, StudentRow studentRow, CellStyle dateStyle) {
        Row row = sheet.createRow(rowNum);
        
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(studentRow.getStudentId());
        
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(studentRow.getFirstName());
        
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(studentRow.getLastName());
        
        Cell cell3 = row.createCell(3);
        cell3.setCellValue(studentRow.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE));
        cell3.setCellStyle(dateStyle);
        
        Cell cell4 = row.createCell(4);
        cell4.setCellValue(studentRow.getClassName());
        
        Cell cell5 = row.createCell(5);
        cell5.setCellValue(studentRow.getScore());
    }
    
    private StudentRow generateRandomStudentRow(long studentId) {
        String firstName = generateRandomFirstName();
        String lastName = generateRandomLastName();
        LocalDate dob = generateRandomDateOfBirth();
        String className = generateRandomClassName();
        int score = generateRandomScore();
        
        return new StudentRow(studentId, firstName, lastName, dob, className, score);
    }
    
    private String generateRandomFirstName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }
    
    private String generateRandomLastName() {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }
    
    private LocalDate generateRandomDateOfBirth() {
        // Generate random date between 2000-01-01 and 2010-12-31
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.of(2010, 12, 31);
        long daysBetween = start.until(end).getDays();
        long randomDays = random.nextLong(daysBetween + 1);
        return start.plusDays(randomDays);
    }
    
    private String generateRandomClassName() {
        return CLASS_NAMES[random.nextInt(CLASS_NAMES.length)];
    }
    
    private int generateRandomScore() {
        return random.nextInt(SCORE_MAX - SCORE_MIN + 1) + SCORE_MIN;
    }
}