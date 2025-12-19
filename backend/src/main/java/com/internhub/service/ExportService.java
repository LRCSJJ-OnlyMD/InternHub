package com.internhub.service;

import com.internhub.model.Internship;
import com.internhub.model.User;
import com.internhub.repository.InternshipRepository;
import com.internhub.repository.UserRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting data to various formats (Excel, CSV). Provides methods
 * to export internships and users.
 */
@Service
public class ExportService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    public ExportService(InternshipRepository internshipRepository, UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Export all internships to Excel format.
     *
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @return Excel file as byte array
     * @throws IOException if export fails
     */
    public byte[] exportInternshipsToExcel(LocalDate fromDate, LocalDate toDate) throws IOException {
        List<Internship> internships;
        if (fromDate != null && toDate != null) {
            internships = internshipRepository.findByCreatedAtBetween(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
        } else {
            internships = internshipRepository.findAll();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Internships");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Title", "Company", "Student", "Student Email", "Instructor",
                "Sector", "Status", "Start Date", "End Date", "Created At", "Submitted At"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // Fill data rows
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Internship internship : internships) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(internship.getId());
                row.createCell(1).setCellValue(internship.getTitle());
                row.createCell(2).setCellValue(internship.getCompanyName());
                row.createCell(3).setCellValue(internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName());
                row.createCell(4).setCellValue(internship.getStudent().getEmail());
                row.createCell(5).setCellValue(internship.getInstructor() != null
                        ? internship.getInstructor().getFirstName() + " " + internship.getInstructor().getLastName() : "");
                row.createCell(6).setCellValue(internship.getSector().getName());
                row.createCell(7).setCellValue(internship.getStatus().toString());
                row.createCell(8).setCellValue(internship.getStartDate().format(formatter));
                row.createCell(9).setCellValue(internship.getEndDate().format(formatter));
                row.createCell(10).setCellValue(internship.getCreatedAt().format(dateTimeFormatter));
                row.createCell(11).setCellValue(internship.getSubmittedAt() != null
                        ? internship.getSubmittedAt().format(dateTimeFormatter) : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export all internships to CSV format.
     *
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @return CSV file as byte array
     * @throws IOException if export fails
     */
    public byte[] exportInternshipsToCSV(LocalDate fromDate, LocalDate toDate) throws IOException {
        List<Internship> internships;
        if (fromDate != null && toDate != null) {
            internships = internshipRepository.findByCreatedAtBetween(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59));
        } else {
            internships = internshipRepository.findAll();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // Write header
            String[] header = {"ID", "Title", "Company", "Student", "Student Email", "Instructor",
                "Sector", "Status", "Start Date", "End Date", "Created At", "Submitted At"};
            writer.writeNext(header);

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Internship internship : internships) {
                String[] data = {
                    String.valueOf(internship.getId()),
                    internship.getTitle(),
                    internship.getCompanyName(),
                    internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                    internship.getStudent().getEmail(),
                    internship.getInstructor() != null
                    ? internship.getInstructor().getFirstName() + " " + internship.getInstructor().getLastName() : "",
                    internship.getSector().getName(),
                    internship.getStatus().toString(),
                    internship.getStartDate().format(formatter),
                    internship.getEndDate().format(formatter),
                    internship.getCreatedAt().format(dateTimeFormatter),
                    internship.getSubmittedAt() != null ? internship.getSubmittedAt().format(dateTimeFormatter) : ""
                };
                writer.writeNext(data);
            }
        }

        return outputStream.toByteArray();
    }

    /**
     * Export all users to Excel format.
     *
     * @return Excel file as byte array
     * @throws IOException if export fails
     */
    public byte[] exportUsersToExcel() throws IOException {
        List<User> users = userRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "First Name", "Last Name", "Email", "Department", "Role",
                "Enabled", "Created At"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill data rows
            int rowNum = 1;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (User user : users) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getDepartment());
                row.createCell(5).setCellValue(user.getRole().name());
                row.createCell(6).setCellValue(user.isEnabled() ? "Yes" : "No");
                row.createCell(7).setCellValue(user.getCreatedAt().format(dateTimeFormatter));
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export users to CSV format.
     *
     * @return CSV file as byte array
     * @throws IOException if export fails
     */
    public byte[] exportUsersToCSV() throws IOException {
        List<User> users = userRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // Write header
            String[] header = {"ID", "First Name", "Last Name", "Email", "Department", "Role",
                "Enabled", "Created At"};
            writer.writeNext(header);

            // Write data
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (User user : users) {
                String[] data = {
                    String.valueOf(user.getId()),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getDepartment(),
                    user.getRole().name(),
                    user.isEnabled() ? "Yes" : "No",
                    user.getCreatedAt().format(dateTimeFormatter)
                };
                writer.writeNext(data);
            }
        }

        return outputStream.toByteArray();
    }
}
