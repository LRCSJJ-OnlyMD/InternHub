package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Enhanced statistics response with comprehensive analytics data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedStatisticsResponse {

    // Overview metrics
    private Long totalInternships;
    private Long activeInternships;
    private Long completedInternships;
    private Long pendingInternships;
    private Long rejectedInternships;

    // User metrics
    private Long totalStudents;
    private Long totalInstructors;
    private Long studentsWithInternships;
    private Long instructorsWithInternships;

    // Performance metrics
    private Double averageInternshipDuration; // in days
    private Double completionRate; // percentage
    private Double approvalRate; // percentage
    private Double rejectionRate; // percentage

    // Time-based statistics
    private List<TimeSeriesData> internshipsOverTime;
    private List<TimeSeriesData> completionsOverTime;

    // Sector breakdown
    private List<StatisticsResponse> internshipsBySector;

    // Status breakdown
    private List<StatisticsResponse> internshipsByStatus;

    // Top performers
    private List<TopPerformerData> topSectors;
    private List<TopPerformerData> topCompanies;
    private List<TopPerformerData> topInstructors;

    // Recent trends
    private TrendData weeklyTrend;
    private TrendData monthlyTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {

        private String period; // e.g., "2024-01", "Week 1"
        private Long count;
        private LocalDate date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformerData {

        private String name;
        private Long count;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {

        private Long currentPeriod;
        private Long previousPeriod;
        private Double changePercentage;
        private String trend; // "UP", "DOWN", "STABLE"
    }
}
