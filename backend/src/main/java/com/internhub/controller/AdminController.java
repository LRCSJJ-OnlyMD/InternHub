package com.internhub.controller;

import com.internhub.dto.InternshipResponse;
import com.internhub.dto.StatisticsResponse;
import com.internhub.model.InternshipStatus;
import com.internhub.model.Sector;
import com.internhub.service.InternshipService;
import com.internhub.service.SectorService;
import com.internhub.service.StatisticsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Admin operations. All methods protected with
 *
 * @PreAuthorize for RBAC. Thin controller - delegates to service layer.
 *
 * Endpoints: - Sector CRUD - Internship management (list all, delete, reassign)
 * - Statistics (by status, by sector) - Advanced search
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final InternshipService internshipService;
    private final SectorService sectorService;
    private final StatisticsService statisticsService;

    public AdminController(InternshipService internshipService,
            SectorService sectorService,
            StatisticsService statisticsService) {
        this.internshipService = internshipService;
        this.sectorService = sectorService;
        this.statisticsService = statisticsService;
    }

    // ========== SECTOR CRUD ==========
    @PostMapping("/sectors")
    public ResponseEntity<Sector> createSector(@Valid @RequestBody Sector sector) {
        Sector created = sectorService.createSector(sector);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/sectors/{id}")
    public ResponseEntity<Sector> updateSector(
            @PathVariable Long id,
            @Valid @RequestBody Sector sector) {
        Sector updated = sectorService.updateSector(id, sector);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/sectors/{id}")
    public ResponseEntity<Void> deleteSector(@PathVariable Long id) {
        sectorService.deleteSector(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sectors")
    @PreAuthorize("isAuthenticated()") // Override class-level restriction - allow all authenticated users
    public ResponseEntity<List<Sector>> getAllSectors() {
        List<Sector> sectors = sectorService.getAllSectors();
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/sectors/{id}")
    @PreAuthorize("isAuthenticated()") // Override class-level restriction - allow all authenticated users
    public ResponseEntity<Sector> getSectorById(@PathVariable Long id) {
        Sector sector = sectorService.getSectorById(id);
        return ResponseEntity.ok(sector);
    }

    // ========== INTERNSHIP MANAGEMENT ==========
    @GetMapping("/internships")
    public ResponseEntity<List<InternshipResponse>> getAllInternships() {
        List<InternshipResponse> internships = internshipService.getAllInternships();
        return ResponseEntity.ok(internships);
    }

    @GetMapping("/internships/{id}")
    public ResponseEntity<InternshipResponse> getInternshipById(@PathVariable Long id) {
        InternshipResponse internship = internshipService.getInternshipById(id);
        return ResponseEntity.ok(internship);
    }

    @DeleteMapping("/internships/{id}")
    public ResponseEntity<Void> deleteInternship(@PathVariable Long id) {
        internshipService.deleteInternship(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internships/{id}/reassign/{instructorId}")
    public ResponseEntity<InternshipResponse> reassignInstructor(
            @PathVariable Long id,
            @PathVariable Long instructorId) {
        InternshipResponse response = internshipService.reassignInstructor(id, instructorId);
        return ResponseEntity.ok(response);
    }

    // ========== ADVANCED SEARCH ==========
    @GetMapping("/internships/search")
    public ResponseEntity<List<InternshipResponse>> searchInternships(
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) InternshipStatus status,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) LocalDate endDateFrom,
            @RequestParam(required = false) LocalDate endDateTo) {

        List<InternshipResponse> results = internshipService.searchInternships(
                sectorId, status, companyName, studentId, instructorId,
                startDateFrom, startDateTo, endDateFrom, endDateTo
        );

        return ResponseEntity.ok(results);
    }

    // ========== STATISTICS ==========
    /**
     * Get internship statistics grouped by status. Uses database aggregation
     * (GROUP BY).
     */
    @GetMapping("/stats/by-status")
    public ResponseEntity<List<StatisticsResponse>> getStatsByStatus() {
        List<StatisticsResponse> stats = statisticsService.getInternshipsByStatus();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get internship statistics grouped by sector. Uses database aggregation
     * (GROUP BY).
     */
    @GetMapping("/stats/by-sector")
    public ResponseEntity<List<StatisticsResponse>> getStatsBySector() {
        List<StatisticsResponse> stats = statisticsService.getInternshipsBySector();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get detailed statistics by status and sector.
     */
    @GetMapping("/stats/detailed")
    public ResponseEntity<List<StatisticsResponse>> getDetailedStats() {
        List<StatisticsResponse> stats = statisticsService.getInternshipsByStatusAndSector();
        return ResponseEntity.ok(stats);
    }
}
