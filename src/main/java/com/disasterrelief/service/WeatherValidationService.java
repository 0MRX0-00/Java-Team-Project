package com.disasterrelief.service;

import com.disasterrelief.model.TransportAsset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * WeatherValidationService evaluates meteorological ceilings:
 * - Heavy Cargo Helicopter: Maximum 7.0
 * - All-Terrain Rescue Truck: Maximum 8.5
 * - Emergency Medical Drone: Maximum 5.0
 * Facilitates safe ground alternative reassignment when aircraft are grounded.
 */
@Service
public class WeatherValidationService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherValidationService.class);

    public static final double HELICOPTER_WEATHER_LIMIT = 7.0;
    public static final double TRUCK_WEATHER_LIMIT = 8.5;
    public static final double DRONE_WEATHER_LIMIT = 5.0;

    public static class SafetyEvaluation {
        private final boolean safe;
        private final boolean aircraftGrounded;
        private final String rejectionReason;
        private final TransportAsset reassignedAsset;
        private final String reassignmentNotes;

        public SafetyEvaluation(boolean safe, boolean aircraftGrounded, String rejectionReason,
                                TransportAsset reassignedAsset, String reassignmentNotes) {
            this.safe = safe;
            this.aircraftGrounded = aircraftGrounded;
            this.rejectionReason = rejectionReason;
            this.reassignedAsset = reassignedAsset;
            this.reassignmentNotes = reassignmentNotes;
        }

        public static SafetyEvaluation approved() {
            return new SafetyEvaluation(true, false, null, null, null);
        }

        public static SafetyEvaluation grounded(String reason, TransportAsset groundAlt, String altNotes) {
            return new SafetyEvaluation(groundAlt != null, true, reason, groundAlt, altNotes);
        }

        public static SafetyEvaluation aborted(String reason) {
            return new SafetyEvaluation(false, false, reason, null, null);
        }

        public boolean isSafe() { return safe; }
        public boolean isAircraftGrounded() { return aircraftGrounded; }
        public String getRejectionReason() { return rejectionReason; }
        public TransportAsset getReassignedAsset() { return reassignedAsset; }
        public String getReassignmentNotes() { return reassignmentNotes; }
    }

    /**
     * Checks if a transport category can operate safely in given weather conditions.
     */
    public boolean isCategoryWeatherPermissible(String category, double weatherIndex) {
        if (category == null) return false;
        switch (category.toUpperCase()) {
            case "HEAVY_CARGO_HELICOPTER":
                return weatherIndex <= HELICOPTER_WEATHER_LIMIT;
            case "ALL_TERRAIN_RESCUE_TRUCK":
                return weatherIndex <= TRUCK_WEATHER_LIMIT;
            case "EMERGENCY_MEDICAL_DRONE":
                return weatherIndex <= DRONE_WEATHER_LIMIT;
            default:
                return weatherIndex <= 5.0;
        }
    }

    /**
     * Evaluates weather safety for candidate asset. If aircraft weather limit is breached,
     * attempts ground reassignment to an available All-Terrain Rescue Truck.
     */
    public SafetyEvaluation evaluateAndFilterWeather(TransportAsset candidateAsset,
                                                      double weatherIndex,
                                                      double requestedPayloadKg,
                                                      double flightDistanceKm,
                                                      List<TransportAsset> allAvailableAssets) {
        if (candidateAsset == null) {
            return SafetyEvaluation.aborted("No feasible transport asset candidate provided");
        }

        String cat = candidateAsset.getCategory();
        double limit = candidateAsset.getWeatherRiskLimit();

        if (weatherIndex <= limit) {
            return SafetyEvaluation.approved();
        }

        // Weather violation occurred
        boolean isAircraft = "HEAVY_CARGO_HELICOPTER".equalsIgnoreCase(cat) || "EMERGENCY_MEDICAL_DRONE".equalsIgnoreCase(cat);
        String reason = String.format("Weather severity %.1f exceeds %s safety limit of %.1f",
                weatherIndex, cat, limit);

        logger.warn("Weather restriction breached for asset [{}]: {}", candidateAsset.getAssetCode(), reason);

        if (isAircraft) {
            // Attempt ground alternative reassignment to All-Terrain Rescue Truck
            Optional<TransportAsset> groundAlt = allAvailableAssets.stream()
                    .filter(a -> "ALL_TERRAIN_RESCUE_TRUCK".equalsIgnoreCase(a.getCategory()))
                    .filter(a -> "AVAILABLE".equalsIgnoreCase(a.getStatus()))
                    .filter(a -> weatherIndex <= a.getWeatherRiskLimit())
                    .filter(a -> a.getMaxPayloadKg() >= requestedPayloadKg)
                    .filter(a -> a.getMaxRangeKm() >= flightDistanceKm)
                    .findFirst();

            if (groundAlt.isPresent()) {
                TransportAsset alt = groundAlt.get();
                String notes = String.format("Aircraft grounded due to weather (%.1f > %.1f). Reassigned safely to All-Terrain Rescue Truck [%s].",
                        weatherIndex, limit, alt.getAssetCode());
                logger.info("Ground alternative found for grounded aircraft: {}", notes);
                return SafetyEvaluation.grounded(reason, alt, notes);
            } else {
                String notes = String.format("Aircraft grounded due to weather (%.1f > %.1f). No feasible ground rescue truck available.",
                        weatherIndex, limit);
                logger.warn("Mission aborted: {}", notes);
                return new SafetyEvaluation(false, true, reason + " (No ground alternative available)", null, notes);
            }
        }

        return SafetyEvaluation.aborted(reason);
    }
}

