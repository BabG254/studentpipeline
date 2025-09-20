package com.studentpipeline.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.studentpipeline.dto.FileOperationResponse;
import com.studentpipeline.entity.Student;
import com.studentpipeline.model.StudentRow;
import com.studentpipeline.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvToDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(CsvToDatabaseService.class);
    private static final int BATCH_SIZE = 5000;
    private static final int PROGRESS_LOG_INTERVAL = 10000;
    private static final int ORIGINAL_EXCEL_SCORE_ADJUSTMENT = 5; // DB score = original Excel score + 5

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Upload CSV file and save students to database with batch processing
     */
    @Transactional
    public FileOperationResponse uploadCsvToDatabase(MultipartFile file) throws IOException, CsvException {
        logger.info("Starting CSV to Database upload for file: {}", file.getOriginalFilename());

        long recordsProcessed = 0;
        long recordsInserted = 0;
        long recordsSkipped = 0;
        long startTime = System.currentTimeMillis();

        List<Student> batch = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext(); // Skip header
            if (header == null) {
                throw new IllegalArgumentException("CSV file is empty or has no header");
            }

            logger.info("CSV header: {}", String.join(", ", header));

            String[] record;
            while ((record = csvReader.readNext()) != null) {
                recordsProcessed++;

                try {
                    StudentRow studentRow = parseCsvRecord(record);
                    if (studentRow != null) {
                        // Calculate database score: original Excel score + 5
                        // Note: CSV might contain Excel+10 if it came from conversion
                        int dbScore = calculateDatabaseScore(studentRow.getScore());
                        studentRow.setScore(dbScore);

                        // Check if student already exists
                        if (!studentRepository.existsByStudentId(studentRow.getStudentId())) {
                            Student student = studentRowToEntity(studentRow);
                            batch.add(student);

                            // Process batch when it reaches the batch size
                            if (batch.size() >= BATCH_SIZE) {
                                recordsInserted += processBatch(batch);
                                batch.clear();
                            }
                        } else {
                            recordsSkipped++;
                            logger.debug("Student with ID {} already exists, skipping", studentRow.getStudentId());
                        }
                    }

                    // Log progress
                    if (recordsProcessed % PROGRESS_LOG_INTERVAL == 0) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        logger.info("Processed {} records in {} ms (inserted: {}, skipped: {})",
                                   recordsProcessed, elapsed, recordsInserted, recordsSkipped);
                    }

                } catch (Exception e) {
                    logger.warn("Error processing CSV record {}: {}", recordsProcessed, e.getMessage());
                    recordsSkipped++;
                }
            }

            // Process remaining batch
            if (!batch.isEmpty()) {
                recordsInserted += processBatch(batch);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("CSV to Database upload completed. {} records processed, {} inserted, {} skipped in {} ms",
                   recordsProcessed, recordsInserted, recordsSkipped, totalTime);

        return new FileOperationResponse(
                "Database",
                file.getOriginalFilename(),
                recordsInserted,
                "CSV_TO_DATABASE"
        );
    }

    private StudentRow parseCsvRecord(String[] record) {
        if (record.length < 6) {
            logger.warn("Invalid CSV record length: expected 6, got {}", record.length);
            return null;
        }

        try {
            Long studentId = Long.parseLong(record[0].trim());
            String firstName = record[1].trim();
            String lastName = record[2].trim();
            LocalDate dob = LocalDate.parse(record[3].trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            String className = record[4].trim();
            Integer score = Integer.parseInt(record[5].trim());

            // Validate required fields
            if (firstName.isEmpty() || lastName.isEmpty() || className.isEmpty()) {
                logger.warn("Empty required fields in CSV record for student {}", studentId);
                return null;
            }

            return new StudentRow(studentId, firstName, lastName, dob, className, score);

        } catch (NumberFormatException | DateTimeParseException e) {
            logger.warn("Error parsing CSV record: {}", e.getMessage());
            return null;
        }
    }

    private int calculateDatabaseScore(int csvScore) {
        // Detect if CSV contains adjusted scores (Excel + 10) or original Excel scores
        // Original Excel scores: 55-75
        // Adjusted CSV scores (Excel + 10): 65-85
        
        if (csvScore >= 65 && csvScore <= 85) {
            // Assume this is an adjusted score (Excel + 10), so calculate: (csvScore - 10) + 5 = csvScore - 5
            int dbScore = csvScore - 5;
            logger.debug("Detected adjusted CSV score {}, calculated DB score: {}", csvScore, dbScore);
            return dbScore;
        } else if (csvScore >= 55 && csvScore <= 75) {
            // Assume this is original Excel score, so calculate: csvScore + 5
            int dbScore = csvScore + ORIGINAL_EXCEL_SCORE_ADJUSTMENT;
            logger.debug("Detected original Excel score {}, calculated DB score: {}", csvScore, dbScore);
            return dbScore;
        } else {
            // Outside expected range, treat as original Excel score
            int dbScore = csvScore + ORIGINAL_EXCEL_SCORE_ADJUSTMENT;
            logger.warn("Unexpected score value {}, treating as original Excel score. DB score: {}", csvScore, dbScore);
            return dbScore;
        }
    }

    private Student studentRowToEntity(StudentRow studentRow) {
        return new Student(
                studentRow.getStudentId(),
                studentRow.getFirstName(),
                studentRow.getLastName(),
                studentRow.getDob(),
                studentRow.getClassName(),
                studentRow.getScore()
        );
    }

    @Transactional
    private int processBatch(List<Student> students) {
        try {
            // Use batch insert with JDBC for better performance
            String sql = "INSERT INTO student (student_id, first_name, last_name, dob, class_name, score, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (student_id) DO NOTHING";

            List<Object[]> batchArgs = new ArrayList<>();
            for (Student student : students) {
                batchArgs.add(new Object[]{
                        student.getStudentId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDob(),
                        student.getClassName(),
                        student.getScore(),
                        student.getCreatedAt()
                });
            }

            int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);
            int insertedCount = 0;
            for (int count : updateCounts) {
                if (count > 0) insertedCount++;
            }

            logger.debug("Batch processed: {} students inserted", insertedCount);
            return insertedCount;

        } catch (Exception e) {
            logger.error("Error processing batch: {}", e.getMessage());
            // Fallback to JPA save
            try {
                List<Student> savedStudents = studentRepository.saveAll(students);
                logger.debug("Fallback batch processed via JPA: {} students", savedStudents.size());
                return savedStudents.size();
            } catch (Exception jpaException) {
                logger.error("JPA fallback also failed: {}", jpaException.getMessage());
                return 0;
            }
        }
    }

    /**
     * Alternative method using PostgreSQL COPY command for maximum performance
     * Note: This requires the CSV to be properly formatted and accessible to PostgreSQL
     */
    @Transactional
    public FileOperationResponse uploadCsvToDatabaseViaCopy(String csvFilePath) {
        logger.info("Starting CSV to Database upload via COPY command for file: {}", csvFilePath);

        try {
            // This would require COPY FROM command, but it's more complex due to file permissions
            // For now, we'll use the batch insert approach which is more reliable
            throw new UnsupportedOperationException("COPY command not implemented yet. Use regular upload method.");

        } catch (Exception e) {
            logger.error("Error with COPY command: {}", e.getMessage());
            throw new RuntimeException("COPY operation failed", e);
        }
    }
}