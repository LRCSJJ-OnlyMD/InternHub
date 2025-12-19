package com.internhub.dto;

/**
 * DTO for statistics response (by status or by sector). Generic structure for
 * aggregated counts.
 */
public class StatisticsResponse {

    private String label;      // Status or Sector name
    private Long count;        // Number of internships

    // Constructors
    public StatisticsResponse() {
    }

    public StatisticsResponse(String label, Long count) {
        this.label = label;
        this.count = count;
    }

    // Getters and Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
