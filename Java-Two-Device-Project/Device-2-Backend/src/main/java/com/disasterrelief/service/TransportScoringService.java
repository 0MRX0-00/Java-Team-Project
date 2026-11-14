package com.disasterrelief.service;

import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.model.ReliefCargo;
import com.disasterrelief.model.TransportAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TransportScoringService implements the exact three-category transport scoring engine:
 * A. Heavy Cargo Helicopter: Affected Pop 40%, Medical Urgency 40%, Terrain Risk (inverse) 20%
 * B. All-Terrain Rescue Truck: Affected Pop 50%, Payload Capacity 30%, Road Accessibility 20%
 * C. Emergency Medical Drone: Critical Supply Urgency 60%, Flight Distance 30%, Payload Weight 10%
 */
@Service
public class TransportScoringService {

    private static final Logger logger = LoggerFactory.getLogger(TransportScoringService.class);

    public static class AssetScoringCandidate {
        private final TransportAsset asset;
        private final double score;
        private final boolean feasible;
        private final String evaluationReason;

        public AssetScoringCandidate(TransportAsset asset, double score, boolean feasible, String evaluationReason) {
            this.asset = asset;
            this.score = score;
            this.feasible = feasible;
            this.evaluationReason = evaluationReason;
        }

        public TransportAsset getAsset() { return asset; }
        public double getScore() { return score; }
        public boolean isFeasible() { return feasible; }
        public String getEvaluationReason() { return evaluationReason; }
    }

    /**
     * Scores a Heavy Cargo Helicopter:
     * - Affected Population: 40% (normalized to 0-100 against 50,000 baseline)
     * - Medical Urgency: 40% (scale 1-10 -> (score/10)*100)
     * - Terrain Risk: 20% (inverse terrain risk: 100 - risk)
     */
    public double scoreHelicopter(DisasterZone zone) {
        double normPop = Math.min(100.0, ((double) zone.getAffectedPopulation() / 50000.0) * 100.0);
        double normUrgency = Math.min(100.0, (zone.getMedicalUrgencyScore() / 10.0) * 100.0);
        double normTerrainInverse = Math.max(0.0, Math.min(100.0, 100.0 - zone.getTerrainRisk()));

        double score = (0.40 * normPop) + (0.40 * normUrgency) + (0.20 * normTerrainInverse);
        return Math.round(score * 10.0) / 10.0;
    }

    /**
     * Scores an All-Terrain Rescue Truck:
     * - Affected Population: 50% (normalized against 50,000 baseline)
     * - Payload Capacity: 30% (requested payload / asset max payload * 100)
     * - Road Accessibility: 20% (scale 0-100)
     */
    public double scoreTruck(DisasterZone zone, ReliefCargo cargo, TransportAsset asset) {
        double normPop = Math.min(100.0, ((double) zone.getAffectedPopulation() / 50000.0) * 100.0);

        double payloadKg = cargo != null ? cargo.getTotalPayloadKg() : 1000.0;
        double normPayload = Math.min(100.0, (payloadKg / asset.getMaxPayloadKg()) * 100.0);

        double normRoad = Math.min(100.0, Math.max(0.0, zone.getRoadAccessibility()));

        double score = (0.50 * normPop) + (0.30 * normPayload) + (0.20 * normRoad);
        return Math.round(score * 10.0) / 10.0;
    }

    /**
     * Scores an Emergency Medical Drone:
     * - Critical Supply Urgency: 60% ((medicalUrgency / 10)*100, boosted if plasma kits requested)
     * - Flight Distance Suitability: 30% (inverse distance suitability within drone range)
     * - Payload Weight Suitability: 10% (inverse weight: lighter payload favors drone speed)
     */
    public double scoreDrone(DisasterZone zone, ReliefCargo cargo, TransportAsset asset) {
        double urgencyFactor = (zone.getMedicalUrgencyScore() / 10.0) * 100.0;
        if (cargo != null && cargo.getPlasmaKitCount() > 0) {
            urgencyFactor = Math.min(100.0, urgencyFactor + 10.0); // Critical medical plasma boost
        }

        // Distance suitability: favors closer target inside max range
        double distanceRatio = zone.getFlightDistanceKm() / asset.getMaxRangeKm();
        double normDistance = Math.max(0.0, Math.min(100.0, (1.0 - distanceRatio) * 100.0));

        // Payload suitability: lighter payload receives higher drone score
        double payloadKg = cargo != null ? cargo.getTotalPayloadKg() : 20.0;
        double payloadRatio = payloadKg / asset.getMaxPayloadKg();
        double normPayload = Math.max(0.0, Math.min(100.0, (1.0 - payloadRatio) * 100.0));

        double score = (0.60 * urgencyFactor) + (0.30 * normDistance) + (0.10 * normPayload);
        return Math.round(score * 10.0) / 10.0;
    }

    /**
     * Evaluates all assets for suitability and feasibility, returning the highest scoring candidate.
     */
    public Optional<AssetScoringCandidate> selectBestFeasibleAsset(DisasterZone zone,
                                                                   ReliefCargo cargo,
                                                                   List<TransportAsset> availableAssets) {
        List<AssetScoringCandidate> candidates = new ArrayList<>();
        double payloadKg = cargo != null ? cargo.getTotalPayloadKg() : 500.0;
        double distanceKm = zone.getFlightDistanceKm();
        double weatherIndex = zone.getWeatherSeverityIndex();

        for (TransportAsset asset : availableAssets) {
            if (!"AVAILABLE".equalsIgnoreCase(asset.getStatus())) {
                candidates.add(new AssetScoringCandidate(asset, 0.0, false, "Asset is currently unavailable (" + asset.getStatus() + ")"));
                continue;
            }

            if (payloadKg > asset.getMaxPayloadKg()) {
                candidates.add(new AssetScoringCandidate(asset, 0.0, false,
                        String.format("Payload %.1f kg exceeds asset capacity %.1f kg", payloadKg, asset.getMaxPayloadKg())));
                continue;
            }

            if (distanceKm > asset.getMaxRangeKm()) {
                candidates.add(new AssetScoringCandidate(asset, 0.0, false,
                        String.format("Flight distance %.1f km exceeds asset max range %.1f km", distanceKm, asset.getMaxRangeKm())));
                continue;
            }

            if (weatherIndex > asset.getWeatherRiskLimit()) {
                candidates.add(new AssetScoringCandidate(asset, 0.0, false,
                        String.format("Weather index %.1f exceeds asset limit %.1f", weatherIndex, asset.getWeatherRiskLimit())));
                continue;
            }

            // Calculate category specific score
            double score = 0.0;
            String category = asset.getCategory();
            if ("HEAVY_CARGO_HELICOPTER".equalsIgnoreCase(category)) {
                score = scoreHelicopter(zone);
            } else if ("ALL_TERRAIN_RESCUE_TRUCK".equalsIgnoreCase(category)) {
                score = scoreTruck(zone, cargo, asset);
            } else if ("EMERGENCY_MEDICAL_DRONE".equalsIgnoreCase(category)) {
                score = scoreDrone(zone, cargo, asset);
            }

            String reason = String.format("Suitability score %.1f computed based on %s parameters (Payload: %.1fkg, Dist: %.1fkm)",
                    score, category, payloadKg, distanceKm);
            candidates.add(new AssetScoringCandidate(asset, score, true, reason));
        }

        // Return highest scoring feasible asset
        return candidates.stream()
                .filter(AssetScoringCandidate::isFeasible)
                .max(Comparator.comparingDouble(AssetScoringCandidate::getScore));
    }
}

