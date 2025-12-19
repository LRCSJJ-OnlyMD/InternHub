package com.internhub.service;

import java.util.List;

import com.internhub.dto.EnhancedStatisticsResponse;
import com.internhub.dto.InstructorStatisticsResponse;
import com.internhub.dto.StatisticsResponse;
import com.internhub.dto.StudentStatisticsResponse;

/**
 * Service interface for statistics operations. Follows SRP - manages ONLY
 * statistics aggregation. Follows ISP - focused interface for statistics only.
 */
public interface StatisticsService {

    /**
     * Get internship count grouped by status. Uses database aggregation (GROUP
     * BY).
     *
     * @return List of statistics (status, count)
     */
    List<StatisticsResponse> getInternshipsByStatus();

    /**
     * Get internship count grouped by sector. Uses database aggregation (GROUP
     * BY).
     *
     * @return List of statistics (sector name, count)
     */
    List<StatisticsResponse> getInternshipsBySector();

    /**
     * Get internship count grouped by status and sector. More detailed
     * breakdown for admin dashboard.
     *
     * @return Nested statistics structure
     */
    List<StatisticsResponse> getInternshipsByStatusAndSector();
    
    /**
     * Get enhanced comprehensive statistics for admin dashboard.
     * Includes overview, trends, top performers, and time series data.
     *
     * @return Enhanced statistics response with all metrics
     */
    EnhancedStatisticsResponse getEnhancedStatistics();
    
    /**
     * Get instructor-specific statistics.
     *
     * @param instructorId ID of the instructor
     * @return Instructor statistics with supervised internships data
     */
    InstructorStatisticsResponse getInstructorStatistics(Long instructorId);
    
    /**
     * Get student-specific statistics.
     *
     * @param studentId ID of the student
     * @return Student statistics with internship history and progress
     */
    StudentStatisticsResponse getStudentStatistics(Long studentId);
}
