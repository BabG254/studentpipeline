package com.studentpipeline.service;

import com.opencsv.CSVWriter;
import com.studentpipeline.config.DataPathConfig;
import com.studentpipeline.dto.FileOperationResponse;
import com.studentpipeline.model.StudentRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class ExcelToCsvService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelToCsvService.class);
    private static final int PROGRESS_LOG_INTERVAL = 10000; // Log every 10K records for better visibility
    private static final int SCORE_ADJUSTMENT = 10; // Add 10 to Excel scores for CSV

    @Autowired
    private DataPathConfig dataPathConfig;
    
    // Static block to configure POI for large files
    static {
        // Allow larger Excel files (500MB limit for 1M+ records)
        IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024);
    }

    /**
     * Convert Excel file to CSV with score adjustment (+10)
     */
    public FileOperationResponse convertExcelToCsv(MultipartFile file) throws IOException {
        logger.info("Starting Excel to CSV conversion for file: {}", file.getOriginalFilename());

        // Ensure data directory exists
        Path dataDir = Paths.get(dataPathConfig.getBase());
        Files.createDirectories(dataDir);

        // Generate CSV file name
        String originalFileName = file.getOriginalFilename();
        String csvFileName = generateCsvFileName(originalFileName);
        Path csvFilePath = dataDir.resolve(csvFileName);

        long recordsProcessed = 0;
        long startTime = System.currentTimeMillis();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             FileWriter fileWriter = new FileWriter(csvFilePath.toFile());
             CSVWriter csvWriter = new CSVWriter(fileWriter,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Write CSV header
            String[] header = {"studentId", "firstName", "lastName", "DOB", "class", "score"};
            csvWriter.writeNext(header);

            // Process data rows
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    StudentRow studentRow = parseRowToStudentRow(row);
                    if (studentRow != null) {
                        // Adjust score: add 10 to original Excel score
                        studentRow.setScore(studentRow.getScore() + SCORE_ADJUSTMENT);
                        
                        String[] csvRow = studentRowToCsvArray(studentRow);
                        csvWriter.writeNext(csvRow);
                        recordsProcessed++;

                        // Log progress
                        if (recordsProcessed % PROGRESS_LOG_INTERVAL == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            logger.info("Processed {} rows in {} ms", recordsProcessed, elapsed);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error processing row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Excel to CSV conversion completed. {} records processed in {} ms",
                   recordsProcessed, totalTime);

        return new FileOperationResponse(
                csvFilePath.toAbsolutePath().toString(),
                csvFileName,
                recordsProcessed,
                "EXCEL_TO_CSV"
        );
    }

    /**
     * Convert Excel file from server path to CSV
     */
    public FileOperationResponse convertExcelToCsvFromPath(String excelFilePath) throws IOException {
        logger.info("Starting Excel to CSV conversion from path: {}", excelFilePath);

        Path excelPath = Paths.get(excelFilePath);
        if (!Files.exists(excelPath)) {
            throw new IOException("Excel file not found: " + excelFilePath);
        }

        // Generate CSV file name
        String csvFileName = generateCsvFileName(excelPath.getFileName().toString());
        Path csvFilePath = excelPath.getParent().resolve(csvFileName);

        long recordsProcessed = 0;
        long startTime = System.currentTimeMillis();

        try (FileInputStream fileInputStream = new FileInputStream(excelPath.toFile());
             Workbook workbook = new XSSFWorkbook(fileInputStream);
             FileWriter fileWriter = new FileWriter(csvFilePath.toFile());
             CSVWriter csvWriter = new CSVWriter(fileWriter,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Write CSV header
            String[] header = {"studentId", "firstName", "lastName", "DOB", "class", "score"};
            csvWriter.writeNext(header);

            // Process data rows
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    StudentRow studentRow = parseRowToStudentRow(row);
                    if (studentRow != null) {
                        // Adjust score: add 10 to original Excel score
                        studentRow.setScore(studentRow.getScore() + SCORE_ADJUSTMENT);
                        
                        String[] csvRow = studentRowToCsvArray(studentRow);
                        csvWriter.writeNext(csvRow);
                        recordsProcessed++;

                        // Log progress
                        if (recordsProcessed % PROGRESS_LOG_INTERVAL == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            logger.info("Processed {} rows in {} ms", recordsProcessed, elapsed);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error processing row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Excel to CSV conversion completed. {} records processed in {} ms",
                   recordsProcessed, totalTime);

        return new FileOperationResponse(
                csvFilePath.toAbsolutePath().toString(),
                csvFileName,
                recordsProcessed,
                "EXCEL_TO_CSV"
        );
    }

    private StudentRow parseRowToStudentRow(Row row) {
        try {
            Long studentId = getCellLongValue(row.getCell(0));
            String firstName = getCellStringValue(row.getCell(1));
            String lastName = getCellStringValue(row.getCell(2));
            LocalDate dob = getCellDateValue(row.getCell(3));
            String className = getCellStringValue(row.getCell(4));
            Integer score = getCellIntegerValue(row.getCell(5));

            if (studentId == null || firstName == null || lastName == null || 
                dob == null || className == null || score == null) {
                logger.warn("Incomplete data in row {}", row.getRowNum());
                return null;
            }

            return new StudentRow(studentId, firstName, lastName, dob, className, score);
        } catch (Exception e) {
            logger.error("Error parsing row {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                return null;
            case STRING:
                try {
                    String dateStr = cell.getStringCellValue().trim();
                    return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    logger.warn("Invalid date format: {}", cell.getStringCellValue());
                    return null;
                }
            default:
                return null;
        }
    }

    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private String[] studentRowToCsvArray(StudentRow studentRow) {
        return new String[]{
                String.valueOf(studentRow.getStudentId()),
                studentRow.getFirstName(),
                studentRow.getLastName(),
                studentRow.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE),
                studentRow.getClassName(),
                String.valueOf(studentRow.getScore())
        };
    }

    private String generateCsvFileName(String originalFileName) {
        if (originalFileName == null) {
            return "processed-students.csv";
        }
        
        String nameWithoutExtension = originalFileName.replaceAll("\\.[^.]*$", "");
        return nameWithoutExtension + "-processed.csv";
    }
}