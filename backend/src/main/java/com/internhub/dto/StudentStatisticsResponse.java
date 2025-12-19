package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Statistics response for student-specific analytics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsResponse {

    // Overview
    private Long totalInternships;
    private Long completedInternships;
    private Long activeInternships;
    private Long pendingInternships;

    // Current internship details
    private InternshipSummary currentInternship;

    // Performance
    private Double completionRate;
    private Integer totalDaysInterned;
    private List<String> sectorsExplored;

    // Timeline
    private List<InternshipTimelineData> internshipTimeline;

    // Progress
    private Integer daysUntilCompletion;
    private Double progressPercentage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipSummary {

        private Long internshipId;
        private String title;
        private String companyName;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private String instructorName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InternshipTimelineData {

        private Long internshipId;
        private String title;
        private String companyName;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer duration; // in days
    }
}
