package com.internhub.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.service.ExportService;

/**
 * REST Controller for data export operations. Provides endpoints for exporting
 * internships and users to Excel and CSV formats. Restricted to ADMIN users
 * only.
 */
@RestController
@RequestMapping("/api/admin/export")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Export internships to Excel or CSV format.
     *
     * @param format Export format (xlsx or csv)
     * @param from Optional start date (format: yyyy-MM-dd)
     * @param to Optional end date (format: yyyy-MM-dd)
     * @return File download response
     */
    @GetMapping("/internships")
    public ResponseEntity<byte[]> exportInternships(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        try {
            LocalDate fromDate = from != null ? LocalDate.parse(from, DateTimeFormatter.ISO_DATE) : null;
            LocalDate toDate = to != null ? LocalDate.parse(to, DateTimeFormatter.ISO_DATE) : null;

            byte[] data;
            String filename;
            String contentType;

            if ("csv".equalsIgnoreCase(format)) {
                data = exportService.exportInternshipsToCSV(fromDate, toDate);
                filename = "internships_" + LocalDate.now() + ".csv";
                contentType = "text/csv";
            } else {
                data = exportService.exportInternshipsToExcel(fromDate, toDate);
                filename = "internships_" + LocalDate.now() + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Export users to Excel or CSV format.
     *
     * @param format Export format (xlsx or csv)
     * @return File download response
     */
    @GetMapping("/users")
    public ResponseEntity<byte[]> exportUsers(@RequestParam(defaultValue = "xlsx") String format) {

        try {
            byte[] data;
            String filename;
            String contentType;

            if ("csv".equalsIgnoreCase(format)) {
                data = exportService.exportUsersToCSV();
                filename = "users_" + LocalDate.now() + ".csv";
                contentType = "text/csv";
            } else {
                data = exportService.exportUsersToExcel();
                filename = "users_" + LocalDate.now() + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
