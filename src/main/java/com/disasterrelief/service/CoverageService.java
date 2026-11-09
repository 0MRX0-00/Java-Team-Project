package com.disasterrelief.service;

import com.disasterrelief.dto.ReliefCoverageDto;
import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.model.FieldConfirmation;
import com.disasterrelief.model.ReliefCargo;
import com.disasterrelief.repository.CargoRepository;
import com.disasterrelief.repository.DisasterZoneRepository;
import com.disasterrelief.repository.FieldConfirmationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CoverageService computes relief delivery fulfillment ratios:
 * ReliefCoverageRatio = (DeliveredRations / TotalRequiredRations) * 100
 * Strictly handles division by zero and aggregates zone-level and overall coverage.
 */
@Service
public class CoverageService {

    private final DisasterZoneRepository zoneRepository;
    private final CargoRepository cargoRepository;
    private final FieldConfirmationRepository confirmationRepository;

    @Autowired
    public CoverageService(DisasterZoneRepository zoneRepository,
                           CargoRepository cargoRepository,
                           FieldConfirmationRepository confirmationRepository) {
        this.zoneRepository = zoneRepository;
        this.cargoRepository = cargoRepository;
        this.confirmationRepository = confirmationRepository;
    }

    /**
     * Calculates relief coverage ratio safely avoiding division by zero.
     */
    public double computeRatio(int deliveredRations, int requiredRations) {
        if (requiredRations <= 0) {
            return deliveredRations > 0 ? 100.0 : 0.0;
        }
        double ratio = ((double) deliveredRations / (double) requiredRations) * 100.0;
        return Math.min(100.0, Math.round(ratio * 100.0) / 100.0);
    }

    /**
     * Generates zone-level coverage metrics for all registered disaster zones.
     */
    public List<ReliefCoverageDto> getZoneCoverages() {
        List<DisasterZone> zones = zoneRepository.findAll();
        List<ReliefCargo> allCargo = cargoRepository.findAll();
        List<FieldConfirmation> confirmations = confirmationRepository.findAll();

        Map<Long, Integer> zoneRequiredMap = allCargo.stream()
                .collect(Collectors.groupingBy(ReliefCargo::getDisasterZoneId,
                        Collectors.summingInt(ReliefCargo::getRequiredRations)));

        // Delivered rations grouped by zone (via mission linkage)
        // Since FieldConfirmation joins mission and zone, map by mission/zone
        List<ReliefCoverageDto> dtos = new ArrayList<>();

        for (DisasterZone z : zones) {
            int required = zoneRequiredMap.getOrDefault(z.getId(), 0);
            int delivered = confirmations.stream()
                    .filter(c -> z.getZoneCode() != null && z.getZoneCode().equalsIgnoreCase(c.getZoneCode()))
                    .mapToInt(FieldConfirmation::getDeliveredRations)
                    .sum();

            double ratio = computeRatio(delivered, required);
            dtos.add(new ReliefCoverageDto(z.getId(), z.getZoneCode(), z.getZoneName(), required, delivered, ratio));
        }

        return dtos;
    }

    /**
     * Computes the global overall relief coverage percentage across all zones.
     */
    public double getOverallCoveragePercentage() {
        List<ReliefCoverageDto> coverages = getZoneCoverages();
        int totalRequired = coverages.stream().mapToInt(ReliefCoverageDto::getTotalRequiredRations).sum();
        int totalDelivered = coverages.stream().mapToInt(ReliefCoverageDto::getTotalDeliveredRations).sum();
        return computeRatio(totalDelivered, totalRequired);
    }
}

