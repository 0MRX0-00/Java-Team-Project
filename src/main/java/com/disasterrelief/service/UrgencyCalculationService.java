package com.disasterrelief.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * UrgencyCalculationService calculates the Disaster Dispatch Urgency Score (DDUS)
 * and determines the strict Priority Classification according to academic assignment rules.
 */
@Service
public class UrgencyCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(UrgencyCalculationService.class);

    // Configurable weights from application.properties
    // Formula: DDUS = (UrgencyScore / 10) * Wu + (AffectedPopulation / 1000) * Wp + (100 - TerrainRisk) * Wt
    private final double weightUrgency;
    private final double weightPopulation;
    private final double weightTerrain;

    public UrgencyCalculationService(
            @Value("${app.ddus.weight-urgency:35.0}") double weightUrgency,
            @Value("${app.ddus.weight-population:35.0}") double weightPopulation,
            @Value("${app.ddus.weight-terrain:30.0}") double weightTerrain) {
        this.weightUrgency = weightUrgency;
        this.weightPopulation = weightPopulation;
        this.weightTerrain = weightTerrain;
        logger.info("UrgencyCalculationService initialized with weights -> Wu: {}, Wp: {}, Wt: {}",
                weightUrgency, weightPopulation, weightTerrain);
    }

    public double getWeightUrgency() { return weightUrgency; }
    public double getWeightPopulation() { return weightPopulation; }
    public double getWeightTerrain() { return weightTerrain; }

    /**
     * Calculates the raw and normalized DDUS score.
     * Normalized components:
     * - Term 1 (Urgency): (medicalUrgency / 10.0) -> [0.0, 1.0] * Wu
     * - Term 2 (Population): Math.min(1.0, (affectedPopulation / 1000.0) / 50.0) -> [0.0, 1.0] * Wp (scaled for 50k benchmark)
     * - Term 3 (Terrain Risk): ((100.0 - terrainRisk) / 100.0) -> [0.0, 1.0] * Wt
     * Clamped to [0.0, 100.0].
     */
    public double calculateDDUS(double medicalUrgency, int affectedPopulation, double terrainRisk) {
        // Medical Urgency component (1 to 10 scale -> 0.1 to 1.0)
        double normUrgency = Math.max(0.0, Math.min(1.0, medicalUrgency / 10.0));
        double termUrgency = normUrgency * weightUrgency;

        // Affected Population component (normalized so up to 50,000 population scales smoothly to 1.0)
        double popScaled = (double) affectedPopulation / 1000.0;
        double normPopulation = Math.max(0.0, Math.min(1.0, popScaled / 50.0));
        double termPopulation = normPopulation * weightPopulation;

        // Terrain Risk component: Inverse terrain risk (100 - risk) normalized to [0, 1]
        double normTerrain = Math.max(0.0, Math.min(1.0, (100.0 - terrainRisk) / 100.0));
        double termTerrain = normTerrain * weightTerrain;

        double totalDdus = termUrgency + termPopulation + termTerrain;

        // Strict clamp to 0.0 - 100.0 range
        double finalScore = Math.max(0.0, Math.min(100.0, Math.round(totalDdus * 10.0) / 10.0));

        logger.debug("DDUS calculation: UrgencyTerm={}/{}, PopTerm={}/{}, TerrainTerm={}/{} => Final DDUS={}",
                termUrgency, weightUrgency, termPopulation, weightPopulation, termTerrain, weightTerrain, finalScore);

        return finalScore;
    }

    /**
     * Determines the Priority Classification based on assignment boundaries:
     * PRIORITY ALPHA: 90–100 (Critical. Immediate lifesaving dispatch)
     * PRIORITY BRAVO: 75–89 (High. Scheduled relief transport)
     * PRIORITY CHARLIE: 60–74 (Moderate. Standard supply replenishment)
     * MISSION ABORTED: Below 60
     */
    public String determinePriorityLevel(double ddusScore) {
        if (ddusScore >= 90.0) {
            return "PRIORITY_ALPHA";
        } else if (ddusScore >= 75.0) {
            return "PRIORITY_BRAVO";
        } else if (ddusScore >= 60.0) {
            return "PRIORITY_CHARLIE";
        } else {
            return "MISSION_ABORTED";
        }
    }
}

