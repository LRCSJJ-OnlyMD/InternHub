package com.internhub.service;

import com.internhub.model.Sector;

import java.util.List;

/**
 * Service interface for Sector operations. Follows SRP - manages ONLY sector
 * business logic. Follows ISP - minimal, focused interface.
 */
public interface SectorService {

    /**
     * Create a new sector.
     *
     * @param sector Sector to create
     * @return Created sector
     */
    Sector createSector(Sector sector);

    /**
     * Update an existing sector.
     *
     * @param id Sector ID
     * @param sector Updated sector data
     * @return Updated sector
     */
    Sector updateSector(Long id, Sector sector);

    /**
     * Delete a sector.
     *
     * @param id Sector ID
     */
    void deleteSector(Long id);

    /**
     * Get sector by ID.
     *
     * @param id Sector ID
     * @return Sector
     */
    Sector getSectorById(Long id);

    /**
     * Get all sectors.
     *
     * @return List of all sectors
     */
    List<Sector> getAllSectors();

    /**
     * Check if sector exists by name.
     *
     * @param name Sector name
     * @return true if exists
     */
    boolean existsByName(String name);
}
