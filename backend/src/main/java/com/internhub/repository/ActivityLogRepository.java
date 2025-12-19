package com.internhub.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.internhub.model.ActivityLog;
import com.internhub.model.User;

/**
 * Repository for ActivityLog operations.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find all activity logs with pagination.
     */
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find activity logs by user with pagination.
     */
    Page<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find activity logs by action type.
     */
    Page<ActivityLog> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);

    /**
     * Find activity logs by entity type and ID.
     */
    Page<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);

    /**
     * Find activity logs within date range.
     */
    @Query("SELECT a FROM ActivityLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find activity logs with multiple filters.
     */
    @Query("SELECT a FROM ActivityLog a WHERE "
            + "(:userEmail IS NULL OR a.userEmail = :userEmail) AND "
            + "(:actionType IS NULL OR a.actionType = :actionType) AND "
            + "(:entityType IS NULL OR a.entityType = :entityType) AND "
            + "(:startDate IS NULL OR a.createdAt >= :startDate) AND "
            + "(:endDate IS NULL OR a.createdAt <= :endDate) "
            + "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findByFilters(@Param("userEmail") String userEmail,
            @Param("actionType") String actionType,
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Count logs by action type.
     */
    long countByActionType(String actionType);

    /**
     * Get distinct action types.
     */
    @Query("SELECT DISTINCT a.actionType FROM ActivityLog a ORDER BY a.actionType")
    List<String> findDistinctActionTypes();

    /**
     * Get distinct entity types.
     */
    @Query("SELECT DISTINCT a.entityType FROM ActivityLog a WHERE a.entityType IS NOT NULL ORDER BY a.entityType")
    List<String> findDistinctEntityTypes();

    /**
     * Delete old logs (for data retention policy).
     */
    @Query("DELETE FROM ActivityLog a WHERE a.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
