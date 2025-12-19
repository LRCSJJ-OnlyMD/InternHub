package com.internhub.service.impl;

import com.internhub.dto.*;
import com.internhub.model.Internship;
import com.internhub.model.InternshipStatus;
import com.internhub.model.Role;
import com.internhub.repository.InternshipRepository;
import com.internhub.repository.UserRepository;
import com.internhub.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StatisticsService. Uses database aggregation queries for
 * efficient statistics. Follows SRP: Manages only statistics operations.
 */
@Service
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    public StatisticsServiceImpl(InternshipRepository internshipRepository, UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<StatisticsResponse> getInternshipsByStatus() {
        List<Object[]> results = internshipRepository.countByStatus();

        return results.stream()
                .map(row -> new StatisticsResponse(
                ((InternshipStatus) row[0]).name(),
                ((Number) row[1]).longValue()
        ))
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsResponse> getInternshipsBySector() {
        List<Object[]> results = internshipRepository.countBySector();

        return results.stream()
                .map(row -> new StatisticsResponse(
                (String) row[0],
                ((Number) row[1]).longValue()
        ))
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsResponse> getInternshipsByStatusAndSector() {
        List<Object[]> results = internshipRepository.countByStatusAndSector();

        return results.stream()
                .map(row -> new StatisticsResponse(
                row[0] + " - " + row[1], // "Sector - Status"
                ((Number) row[2]).longValue()
        ))
                .collect(Collectors.toList());
    }

    @Override
    public EnhancedStatisticsResponse getEnhancedStatistics() {
        EnhancedStatisticsResponse response = new EnhancedStatisticsResponse();

        // Overview metrics
        response.setTotalInternships(internshipRepository.count());
        response.setActiveInternships(internshipRepository.countByStatus(InternshipStatus.IN_PROGRESS));
        response.setCompletedInternships(internshipRepository.countByStatus(InternshipStatus.COMPLETED));
        response.setPendingInternships(internshipRepository.countByStatus(InternshipStatus.PENDING));
        response.setRejectedInternships(internshipRepository.countByStatus(InternshipStatus.REJECTED));

        // User metrics
        response.setTotalStudents(userRepository.countByRole(Role.STUDENT));
        response.setTotalInstructors(userRepository.countByRole(Role.INSTRUCTOR));
        response.setStudentsWithInternships(internshipRepository.countDistinctStudents());
        response.setInstructorsWithInternships(internshipRepository.countDistinctInstructors());

        // Performance metrics
        Double avgDuration = internshipRepository.getAverageDuration();
        response.setAverageInternshipDuration(avgDuration != null ? avgDuration : 0.0);

        long total = response.getTotalInternships();
        if (total > 0) {
            response.setCompletionRate((response.getCompletedInternships() * 100.0) / total);
            response.setApprovalRate(((response.getCompletedInternships() + response.getActiveInternships()) * 100.0) / total);
            response.setRejectionRate((response.getRejectedInternships() * 100.0) / total);
        } else {
            response.setCompletionRate(0.0);
            response.setApprovalRate(0.0);
            response.setRejectionRate(0.0);
        }

        // Breakdown statistics
        response.setInternshipsBySector(getInternshipsBySector());
        response.setInternshipsByStatus(getInternshipsByStatus());

        // Top performers
        response.setTopSectors(getTopSectors());
        response.setTopCompanies(getTopCompanies());
        response.setTopInstructors(getTopInstructors());

        // Trends
        response.setWeeklyTrend(calculateWeeklyTrend());
        response.setMonthlyTrend(calculateMonthlyTrend());

        // Time series (last 6 months)
        response.setInternshipsOverTime(getInternshipsOverTime());
        response.setCompletionsOverTime(getCompletionsOverTime());

        return response;
    }

    @Override
    public InstructorStatisticsResponse getInstructorStatistics(Long instructorId) {
        InstructorStatisticsResponse response = new InstructorStatisticsResponse();

        // Overview
        response.setTotalAssignedInternships(internshipRepository.countByInstructorId(instructorId).longValue());
        response.setActiveInternships(internshipRepository.countByInstructorIdAndStatus(instructorId, InternshipStatus.IN_PROGRESS));
        response.setCompletedInternships(internshipRepository.countByInstructorIdAndStatus(instructorId, InternshipStatus.COMPLETED));
        response.setPendingValidation(internshipRepository.countByInstructorIdAndStatus(instructorId, InternshipStatus.PENDING));

        // Performance
        long total = response.getTotalAssignedInternships();
        if (total > 0) {
            response.setCompletionRate((response.getCompletedInternships() * 100.0) / total);
        } else {
            response.setCompletionRate(0.0);
        }

        // Average validation time (simplified - could be enhanced with actual timestamps)
        response.setAverageValidationTime(3.5); // Placeholder

        // Total comments by instructor
        response.setTotalComments(0); // Would need Comment repository query

        // Breakdown
        List<Internship> internships = internshipRepository.findByInstructorId(instructorId);
        response.setInternshipsByStatus(getInternshipsByStatusForList(internships));
        response.setInternshipsBySector(getInternshipsBySectorForList(internships));

        // Students supervised
        response.setTotalStudentsSupervised(internshipRepository.countDistinctStudentsByInstructor(instructorId));
        response.setTopStudents(getTopStudentsForInstructor(instructorId));

        return response;
    }

    @Override
    public StudentStatisticsResponse getStudentStatistics(Long studentId) {
        StudentStatisticsResponse response = new StudentStatisticsResponse();

        List<Internship> internships = internshipRepository.findByStudentId(studentId);

        // Overview
        response.setTotalInternships((long) internships.size());
        response.setCompletedInternships(internships.stream()
                .filter(i -> i.getStatus() == InternshipStatus.COMPLETED)
                .count());
        response.setActiveInternships(internships.stream()
                .filter(i -> i.getStatus() == InternshipStatus.IN_PROGRESS)
                .count());
        response.setPendingInternships(internships.stream()
                .filter(i -> i.getStatus() == InternshipStatus.PENDING)
                .count());

        // Current internship
        Internship currentInternship = internships.stream()
                .filter(i -> i.getStatus() == InternshipStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        if (currentInternship != null) {
            StudentStatisticsResponse.InternshipSummary summary = new StudentStatisticsResponse.InternshipSummary();
            summary.setInternshipId(currentInternship.getId());
            summary.setTitle(currentInternship.getTitle());
            summary.setCompanyName(currentInternship.getCompanyName());
            summary.setStatus(currentInternship.getStatus().name());
            summary.setStartDate(currentInternship.getStartDate());
            summary.setEndDate(currentInternship.getEndDate());
            if (currentInternship.getInstructor() != null) {
                summary.setInstructorName(currentInternship.getInstructor().getFirstName() + " "
                        + currentInternship.getInstructor().getLastName());
            }
            response.setCurrentInternship(summary);

            // Progress calculation
            long totalDays = ChronoUnit.DAYS.between(currentInternship.getStartDate(), currentInternship.getEndDate());
            long daysPassed = ChronoUnit.DAYS.between(currentInternship.getStartDate(), LocalDate.now());
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), currentInternship.getEndDate());

            response.setDaysUntilCompletion((int) daysRemaining);
            response.setProgressPercentage(totalDays > 0 ? (daysPassed * 100.0) / totalDays : 0.0);
        } else {
            response.setDaysUntilCompletion(0);
            response.setProgressPercentage(0.0);
        }

        // Performance
        long total = response.getTotalInternships();
        response.setCompletionRate(total > 0 ? (response.getCompletedInternships() * 100.0) / total : 0.0);

        Integer totalDays = internshipRepository.getTotalDaysInternedByStudent(studentId);
        response.setTotalDaysInterned(totalDays != null ? totalDays : 0);

        List<String> sectors = internshipRepository.getDistinctSectorsByStudent(studentId);
        response.setSectorsExplored(sectors);

        // Timeline
        List<StudentStatisticsResponse.InternshipTimelineData> timeline = internships.stream()
                .sorted(Comparator.comparing(Internship::getStartDate).reversed())
                .map(i -> {
                    StudentStatisticsResponse.InternshipTimelineData data
                            = new StudentStatisticsResponse.InternshipTimelineData();
                    data.setInternshipId(i.getId());
                    data.setTitle(i.getTitle());
                    data.setCompanyName(i.getCompanyName());
                    data.setStatus(i.getStatus().name());
                    data.setStartDate(i.getStartDate());
                    data.setEndDate(i.getEndDate());
                    data.setDuration((int) ChronoUnit.DAYS.between(i.getStartDate(), i.getEndDate()));
                    return data;
                })
                .collect(Collectors.toList());
        response.setInternshipTimeline(timeline);

        return response;
    }

    // Helper methods
    private List<EnhancedStatisticsResponse.TopPerformerData> getTopSectors() {
        List<Object[]> results = internshipRepository.countBySector();
        long total = internshipRepository.count();

        return results.stream()
                .limit(5)
                .map(row -> new EnhancedStatisticsResponse.TopPerformerData(
                (String) row[0],
                ((Number) row[1]).longValue(),
                total > 0 ? (((Number) row[1]).longValue() * 100.0) / total : 0.0
        ))
                .collect(Collectors.toList());
    }

    private List<EnhancedStatisticsResponse.TopPerformerData> getTopCompanies() {
        List<Object[]> results = internshipRepository.getTopCompaniesByCount();
        long total = internshipRepository.count();

        return results.stream()
                .limit(5)
                .map(row -> new EnhancedStatisticsResponse.TopPerformerData(
                (String) row[0],
                ((Number) row[1]).longValue(),
                total > 0 ? (((Number) row[1]).longValue() * 100.0) / total : 0.0
        ))
                .collect(Collectors.toList());
    }

    private List<EnhancedStatisticsResponse.TopPerformerData> getTopInstructors() {
        List<Object[]> results = internshipRepository.getTopInstructorsByCount();
        long total = internshipRepository.count();

        return results.stream()
                .limit(5)
                .map(row -> new EnhancedStatisticsResponse.TopPerformerData(
                row[0] + " " + row[1],
                ((Number) row[2]).longValue(),
                total > 0 ? (((Number) row[2]).longValue() * 100.0) / total : 0.0
        ))
                .collect(Collectors.toList());
    }

    private EnhancedStatisticsResponse.TrendData calculateWeeklyTrend() {
        LocalDate now = LocalDate.now();
        LocalDate weekAgo = now.minusWeeks(1);
        LocalDate twoWeeksAgo = now.minusWeeks(2);

        long currentWeek = internshipRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null
                && i.getCreatedAt().toLocalDate().isAfter(weekAgo))
                .count();

        long previousWeek = internshipRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null
                && i.getCreatedAt().toLocalDate().isAfter(twoWeeksAgo)
                && i.getCreatedAt().toLocalDate().isBefore(weekAgo))
                .count();

        return calculateTrend(currentWeek, previousWeek);
    }

    private EnhancedStatisticsResponse.TrendData calculateMonthlyTrend() {
        LocalDate now = LocalDate.now();
        LocalDate monthAgo = now.minusMonths(1);
        LocalDate twoMonthsAgo = now.minusMonths(2);

        long currentMonth = internshipRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null
                && i.getCreatedAt().toLocalDate().isAfter(monthAgo))
                .count();

        long previousMonth = internshipRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null
                && i.getCreatedAt().toLocalDate().isAfter(twoMonthsAgo)
                && i.getCreatedAt().toLocalDate().isBefore(monthAgo))
                .count();

        return calculateTrend(currentMonth, previousMonth);
    }

    private EnhancedStatisticsResponse.TrendData calculateTrend(long current, long previous) {
        double changePercentage = 0.0;
        String trend = "STABLE";

        if (previous > 0) {
            changePercentage = ((current - previous) * 100.0) / previous;
            if (changePercentage > 5) {
                trend = "UP";
            } else if (changePercentage < -5) {
                trend = "DOWN";
            }
        } else if (current > 0) {
            changePercentage = 100.0;
            trend = "UP";
        }

        return new EnhancedStatisticsResponse.TrendData(current, previous, changePercentage, trend);
    }

    private List<EnhancedStatisticsResponse.TimeSeriesData> getInternshipsOverTime() {
        List<Internship> internships = internshipRepository.findAll();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        return internships.stream()
                .filter(i -> i.getCreatedAt() != null
                && i.getCreatedAt().toLocalDate().isAfter(sixMonthsAgo))
                .collect(Collectors.groupingBy(
                        i -> i.getCreatedAt().toLocalDate().withDayOfMonth(1).toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new EnhancedStatisticsResponse.TimeSeriesData(
                e.getKey(),
                e.getValue(),
                LocalDate.parse(e.getKey())
        ))
                .sorted(Comparator.comparing(EnhancedStatisticsResponse.TimeSeriesData::getDate))
                .collect(Collectors.toList());
    }

    private List<EnhancedStatisticsResponse.TimeSeriesData> getCompletionsOverTime() {
        List<Internship> internships = internshipRepository.findByStatus(InternshipStatus.COMPLETED);
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);

        return internships.stream()
                .filter(i -> i.getEndDate() != null && i.getEndDate().isAfter(sixMonthsAgo))
                .collect(Collectors.groupingBy(
                        i -> i.getEndDate().withDayOfMonth(1).toString(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new EnhancedStatisticsResponse.TimeSeriesData(
                e.getKey(),
                e.getValue(),
                LocalDate.parse(e.getKey())
        ))
                .sorted(Comparator.comparing(EnhancedStatisticsResponse.TimeSeriesData::getDate))
                .collect(Collectors.toList());
    }

    private List<StatisticsResponse> getInternshipsByStatusForList(List<Internship> internships) {
        return internships.stream()
                .collect(Collectors.groupingBy(Internship::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new StatisticsResponse(e.getKey().name(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<StatisticsResponse> getInternshipsBySectorForList(List<Internship> internships) {
        return internships.stream()
                .collect(Collectors.groupingBy(i -> i.getSector().getName(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new StatisticsResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<InstructorStatisticsResponse.StudentPerformanceData> getTopStudentsForInstructor(Long instructorId) {
        List<Internship> internships = internshipRepository.findByInstructorId(instructorId);

        return internships.stream()
                .collect(Collectors.groupingBy(Internship::getStudent))
                .entrySet().stream()
                .map(e -> {
                    long completed = e.getValue().stream()
                            .filter(i -> i.getStatus() == InternshipStatus.COMPLETED)
                            .count();
                    String currentStatus = e.getValue().stream()
                            .filter(i -> i.getStatus() == InternshipStatus.IN_PROGRESS)
                            .findFirst()
                            .map(i -> i.getStatus().name())
                            .orElse("NONE");

                    return new InstructorStatisticsResponse.StudentPerformanceData(
                            e.getKey().getId(),
                            e.getKey().getFirstName() + " " + e.getKey().getLastName(),
                            (int) completed,
                            currentStatus
                    );
                })
                .sorted(Comparator.comparing(InstructorStatisticsResponse.StudentPerformanceData::getInternshipsCompleted).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}
