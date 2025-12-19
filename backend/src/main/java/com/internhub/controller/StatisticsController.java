package com.internhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.dto.EnhancedStatisticsResponse;
import com.internhub.dto.InstructorStatisticsResponse;
import com.internhub.dto.StatisticsResponse;
import com.internhub.dto.StudentStatisticsResponse;
import com.internhub.service.StatisticsService;

/**
 * REST Controller for statistics operations. Only admins can access statistics.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Get internship counts grouped by status.
     */
    @GetMapping("/by-status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<StatisticsResponse>> getInternshipsByStatus() {
        List<StatisticsResponse> statistics = statisticsService.getInternshipsByStatus();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get internship counts grouped by sector.
     */
    @GetMapping("/by-sector")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<StatisticsResponse>> getInternshipsBySector() {
        List<StatisticsResponse> statistics = statisticsService.getInternshipsBySector();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get detailed breakdown by status and sector.
     */
    @GetMapping("/by-status-and-sector")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<StatisticsResponse>> getInternshipsByStatusAndSector() {
        List<StatisticsResponse> statistics = statisticsService.getInternshipsByStatusAndSector();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get enhanced comprehensive statistics for admin dashboard.
     */
    @GetMapping("/enhanced")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<EnhancedStatisticsResponse> getEnhancedStatistics() {
        EnhancedStatisticsResponse statistics = statisticsService.getEnhancedStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get instructor-specific statistics.
     */
    @GetMapping("/instructor/{instructorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<InstructorStatisticsResponse> getInstructorStatistics(
            @PathVariable Long instructorId) {
        InstructorStatisticsResponse statistics = statisticsService.getInstructorStatistics(instructorId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get student-specific statistics.
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    public ResponseEntity<StudentStatisticsResponse> getStudentStatistics(
            @PathVariable Long studentId) {
        StudentStatisticsResponse statistics = statisticsService.getStudentStatistics(studentId);
        return ResponseEntity.ok(statistics);
    }
}
