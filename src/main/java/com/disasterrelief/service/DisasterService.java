package com.disasterrelief.service;

import com.disasterrelief.dto.DisasterZoneRequest;
import com.disasterrelief.exception.ResourceNotFoundException;
import com.disasterrelief.exception.ValidationException;
import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.repository.DisasterZoneRepository;
import com.disasterrelief.utility.ZoneCodeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DisasterService {

    private final DisasterZoneRepository zoneRepository;

    @Autowired
    public DisasterService(DisasterZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public DisasterZone registerDisasterZone(DisasterZoneRequest req) {
        // Enforce Pattern & Matcher validation
        if (!ZoneCodeValidator.isValid(req.getZoneCode())) {
            throw new ValidationException("Disaster Zone Code does not match required format ZONE-[A-Z]{2}-\\d{4}: " + req.getZoneCode());
        }

        if (zoneRepository.findByZoneCode(req.getZoneCode()).isPresent()) {
            throw new ValidationException("Disaster Zone Code already exists: " + req.getZoneCode());
        }

        DisasterZone zone = new DisasterZone();
        zone.setZoneCode(req.getZoneCode().trim().toUpperCase());
        zone.setZoneName(req.getZoneName().trim());
        zone.setEventType(req.getEventType().trim().toUpperCase());
        zone.setAffectedPopulation(req.getAffectedPopulation());
        zone.setMedicalUrgencyScore(req.getMedicalUrgencyScore());
        zone.setTerrainRisk(req.getTerrainRisk());
        zone.setRoadAccessibility(req.getRoadAccessibility());
        zone.setWeatherSeverityIndex(req.getWeatherSeverityIndex());
        zone.setFlightDistanceKm(req.getFlightDistanceKm());
        zone.setStatus("ACTIVE");

        return zoneRepository.save(zone);
    }

    public List<DisasterZone> getAllZones() {
        return zoneRepository.findAll();
    }

    public DisasterZone getZoneById(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster Zone not found with ID: " + id));
    }

    public DisasterZone updateZone(Long id, DisasterZoneRequest req) {
        DisasterZone zone = getZoneById(id);
        zone.setZoneName(req.getZoneName().trim());
        zone.setEventType(req.getEventType().trim().toUpperCase());
        zone.setAffectedPopulation(req.getAffectedPopulation());
        zone.setMedicalUrgencyScore(req.getMedicalUrgencyScore());
        zone.setTerrainRisk(req.getTerrainRisk());
        zone.setRoadAccessibility(req.getRoadAccessibility());
        zone.setWeatherSeverityIndex(req.getWeatherSeverityIndex());
        zone.setFlightDistanceKm(req.getFlightDistanceKm());
        zoneRepository.update(zone);
        return zone;
    }
}

