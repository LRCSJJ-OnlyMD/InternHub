package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Statistics response for instructor-specific analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorStatisticsResponse {

    // Overview
    private Long totalAssignedInternships;
    private Long activeInternships;
    private Long completedInternships;
    private Long pendingValidation;

    // Performance
    private Double averageValidationTime; // in days
    private Double completionRate;
    private Integer totalComments;

    // Breakdown
    private List<StatisticsResponse> internshipsByStatus;
    private List<StatisticsResponse> internshipsBySector;

    // Students supervised
    private Long totalStudentsSupervised;
    private List<StudentPerformanceData> topStudents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPerformanceData {

        private Long studentId;
        private String studentName;
        private Integer internshipsCompleted;
        private String currentStatus;
    }
}
