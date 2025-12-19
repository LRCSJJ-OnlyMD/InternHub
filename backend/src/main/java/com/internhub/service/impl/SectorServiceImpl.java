package com.internhub.service.impl;

import com.internhub.model.Sector;
import com.internhub.repository.SectorRepository;
import com.internhub.service.SectorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of SectorService. Follows SRP: Manages only sector operations.
 */
@Service
@Transactional
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;

    public SectorServiceImpl(SectorRepository sectorRepository) {
        this.sectorRepository = sectorRepository;
    }

    @Override
    public Sector createSector(Sector sector) {
        if (sectorRepository.existsByNameIgnoreCase(sector.getName())) {
            throw new RuntimeException("Sector with name '" + sector.getName() + "' already exists");
        }
        return sectorRepository.save(sector);
    }

    @Override
    public Sector updateSector(Long id, Sector sector) {
        Sector existing = sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector not found"));

        // Check if new name conflicts with existing sector
        if (!existing.getName().equalsIgnoreCase(sector.getName())
                && sectorRepository.existsByNameIgnoreCase(sector.getName())) {
            throw new RuntimeException("Sector with name '" + sector.getName() + "' already exists");
        }

        existing.setName(sector.getName());
        existing.setDescription(sector.getDescription());

        return sectorRepository.save(existing);
    }

    @Override
    public void deleteSector(Long id) {
        if (!sectorRepository.existsById(id)) {
            throw new RuntimeException("Sector not found");
        }
        sectorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Sector getSectorById(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sector> getAllSectors() {
        return sectorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sector getSectorByName(String name) {
        return sectorRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Sector not found: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return sectorRepository.existsByNameIgnoreCase(name);
    }
}
