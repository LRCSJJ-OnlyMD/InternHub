package com.internhub.repository;

import com.internhub.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Sector entity. Follows Interface Segregation Principle (ISP) -
 * minimal, focused interface.
 */
@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    /**
     * Find sector by name (case-insensitive).
     */
    Optional<Sector> findByNameIgnoreCase(String name);

    /**
     * Find sector by code (case-insensitive).
     */
    Optional<Sector> findByCode(String code);

    /**
     * Check if sector exists by name.
     */
    boolean existsByNameIgnoreCase(String name);
}
