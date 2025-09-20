package com.studentpipeline.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import com.studentpipeline.dto.StudentDto;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    /**
     * Export students to Excel format
     */
    public ResponseEntity<Resource> exportToExcel(List<StudentDto> students, String fileName) throws IOException {
        logger.info("Exporting {} students to Excel", students.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             SXSSFWorkbook workbook = new SXSSFWorkbook(1000)) {

            SXSSFSheet sheet = workbook.createSheet("Students Report");

            // Create date format for DOB column
            CellStyle dateStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            dateStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd"));

            // Create header row
            createExcelHeaderRow(sheet);

            // Create data rows
            int rowNum = 1;
            for (StudentDto student : students) {
                createExcelDataRow(sheet, rowNum++, student, dateStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 7; i++) {
                sheet.trackColumnForAutoSizing(i);
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            workbook.dispose();

            byte[] data = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(data);

            String finalFileName = fileName != null ? fileName : "students_report.xlsx";
            if (!finalFileName.endsWith(".xlsx")) {
                finalFileName += ".xlsx";
            }

            logger.info("Excel export completed. File size: {} bytes", data.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        }
    }

    /**
     * Export students to CSV format
     */
    public ResponseEntity<Resource> exportToCsv(List<StudentDto> students, String fileName) throws IOException {
        logger.info("Exporting {} students to CSV", students.size());

        try (StringWriter stringWriter = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(stringWriter,
                     CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.NO_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            // Write header
            String[] header = {"ID", "StudentID", "FirstName", "LastName", "DOB", "Class", "Score", "CreatedAt"};
            csvWriter.writeNext(header);

            // Write data rows
            for (StudentDto student : students) {
                String[] row = {
                        student.getId() != null ? student.getId().toString() : "",
                        student.getStudentId().toString(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        student.getClassName(),
                        student.getScore().toString(),
                        student.getCreatedAt() != null ? 
                            student.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                };
                csvWriter.writeNext(row);
            }

            byte[] data = stringWriter.toString().getBytes();
            ByteArrayResource resource = new ByteArrayResource(data);

            String finalFileName = fileName != null ? fileName : "students_report.csv";
            if (!finalFileName.endsWith(".csv")) {
                finalFileName += ".csv";
            }

            logger.info("CSV export completed. File size: {} bytes", data.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFileName + "\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(data.length)
                    .body(resource);
        }
    }

    /**
     * Export students to PDF format
     */
    public ResponseEntity<Resource> exportToPdf(List<StudentDto> students, String fileName) throws IOException {
        logger.info("Exporting {} students to PDF", students.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add title
            Paragraph title = new Paragraph("Students Report")
                    .setFontSize(20)
                    .setBold();
            document.add(title);

            // Create table with 7 columns
            Table table = new Table(UnitValue.createPercentArray(new float[]{5, 10, 15, 15, 12, 10, 8, 25}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Add header cells
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Student ID").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("First Name").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Last Name").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("DOB").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Class").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Score").setBold()));
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Created At").setBold()));

            // Add data rows
            for (StudentDto student : students) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getId() != null ? student.getId().toString() : "")));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getStudentId().toString())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getFirstName())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getLastName())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE))));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getClassName())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getScore().toString())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(
                        student.getCreatedAt() != null ? 
                            student.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")));
            }

            document.add(table);
            document.close();

            byte[] data = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(data);

            String finalFileName = fileName != null ? fileName : "students_report.pdf";
            if (!finalFileName.endsWith(".pdf")) {
                finalFileName += ".pdf";
            }

            logger.info("PDF export completed. File size: {} bytes", data.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(data.length)
                    .body(resource);
        }
    }

    private void createExcelHeaderRow(SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);

        org.apache.poi.ss.usermodel.Cell cell0 = headerRow.createCell(0);
        cell0.setCellValue("ID");

        org.apache.poi.ss.usermodel.Cell cell1 = headerRow.createCell(1);
        cell1.setCellValue("Student ID");

        org.apache.poi.ss.usermodel.Cell cell2 = headerRow.createCell(2);
        cell2.setCellValue("First Name");

        org.apache.poi.ss.usermodel.Cell cell3 = headerRow.createCell(3);
        cell3.setCellValue("Last Name");

        org.apache.poi.ss.usermodel.Cell cell4 = headerRow.createCell(4);
        cell4.setCellValue("DOB");

        org.apache.poi.ss.usermodel.Cell cell5 = headerRow.createCell(5);
        cell5.setCellValue("Class");

        org.apache.poi.ss.usermodel.Cell cell6 = headerRow.createCell(6);
        cell6.setCellValue("Score");

        org.apache.poi.ss.usermodel.Cell cell7 = headerRow.createCell(7);
        cell7.setCellValue("Created At");
    }

    private void createExcelDataRow(SXSSFSheet sheet, int rowNum, StudentDto student, CellStyle dateStyle) {
        Row row = sheet.createRow(rowNum);

        org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
        if (student.getId() != null) {
            cell0.setCellValue(student.getId());
        }

        org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
        cell1.setCellValue(student.getStudentId());

        org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
        cell2.setCellValue(student.getFirstName());

        org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
        cell3.setCellValue(student.getLastName());

        org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
        cell4.setCellValue(student.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE));
        cell4.setCellStyle(dateStyle);

        org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
        cell5.setCellValue(student.getClassName());

        org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
        cell6.setCellValue(student.getScore());

        org.apache.poi.ss.usermodel.Cell cell7 = row.createCell(7);
        if (student.getCreatedAt() != null) {
            cell7.setCellValue(student.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }
}