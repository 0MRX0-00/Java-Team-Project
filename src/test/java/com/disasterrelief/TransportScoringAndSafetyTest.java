package com.disasterrelief;

import com.disasterrelief.model.DisasterZone;
import com.disasterrelief.model.ReliefCargo;
import com.disasterrelief.model.TransportAsset;
import com.disasterrelief.service.TransportScoringService;
import com.disasterrelief.service.WeatherValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transport Scoring Engine & Weather Hazard Safety Filter Tests")
class TransportScoringAndSafetyTest {

    private TransportScoringService scoringService;
    private WeatherValidationService weatherService;

    @BeforeEach
    void setUp() {
        scoringService = new TransportScoringService();
        weatherService = new WeatherValidationService();
    }

    @Test
    @DisplayName("Should score Heavy Cargo Helicopter based on 40/40/20 weights")
    void testHelicopterScoring() {
        DisasterZone zone = new DisasterZone();
        zone.setAffectedPopulation(50000); // 100% pop
        zone.setMedicalUrgencyScore(10.0);  // 100% urgency
        zone.setTerrainRisk(0.0);           // 100% inverse terrain

        double score = scoringService.scoreHelicopter(zone);
        assertEquals(100.0, score, 0.5);

        // Lower terrain score (higher risk)
        zone.setTerrainRisk(50.0); // 50% inverse terrain
        double score2 = scoringService.scoreHelicopter(zone);
        // (0.40 * 100) + (0.40 * 100) + (0.20 * 50) = 40 + 40 + 10 = 90
        assertEquals(90.0, score2, 0.5);
    }

    @Test
    @DisplayName("Should score All-Terrain Rescue Truck based on 50/30/20 weights")
    void testTruckScoring() {
        DisasterZone zone = new DisasterZone();
        zone.setAffectedPopulation(50000); // 100%
        zone.setRoadAccessibility(100.0);  // 100%

        ReliefCargo cargo = new ReliefCargo();
        cargo.setTotalPayloadKg(5000.0);

        TransportAsset truck = new TransportAsset();
        truck.setMaxPayloadKg(5000.0); // 100% payload capacity utilization

        double score = scoringService.scoreTruck(zone, cargo, truck);
        // (0.50 * 100) + (0.30 * 100) + (0.20 * 100) = 100.0
        assertEquals(100.0, score, 0.5);
    }

    @Test
    @DisplayName("Should score Emergency Medical Drone based on 60/30/10 weights with plasma bonus")
    void testDroneScoringWithPlasma() {
        DisasterZone zone = new DisasterZone();
        zone.setMedicalUrgencyScore(9.0);
        zone.setFlightDistanceKm(50.0);

        ReliefCargo cargo = new ReliefCargo();
        cargo.setPlasmaKitCount(20); // Plasma present
        cargo.setTotalPayloadKg(15.0);

        TransportAsset drone = new TransportAsset();
        drone.setMaxRangeKm(150.0);
        drone.setMaxPayloadKg(50.0);

        double score = scoringService.scoreDrone(zone, cargo, drone);
        assertTrue(score > 80.0, "Drone score should be high for critical plasma delivery, was: " + score);
    }

    @Test
    @DisplayName("Should enforce weather ceilings: Heli <= 7.0, Truck <= 8.5, Drone <= 5.0")
    void testWeatherCeilings() {
        assertTrue(weatherService.isCategoryWeatherPermissible("HEAVY_CARGO_HELICOPTER", 6.8));
        assertFalse(weatherService.isCategoryWeatherPermissible("HEAVY_CARGO_HELICOPTER", 7.2));

        assertTrue(weatherService.isCategoryWeatherPermissible("ALL_TERRAIN_RESCUE_TRUCK", 8.2));
        assertFalse(weatherService.isCategoryWeatherPermissible("ALL_TERRAIN_RESCUE_TRUCK", 8.8));

        assertTrue(weatherService.isCategoryWeatherPermissible("EMERGENCY_MEDICAL_DRONE", 4.5));
        assertFalse(weatherService.isCategoryWeatherPermissible("EMERGENCY_MEDICAL_DRONE", 5.5));
    }

    @Test
    @DisplayName("Should ground helicopter and reassign to All-Terrain Truck when weather is between 7.0 and 8.5")
    void testGroundReassignment() {
        TransportAsset heli = new TransportAsset();
        heli.setAssetCode("HELI-01");
        heli.setCategory("HEAVY_CARGO_HELICOPTER");
        heli.setWeatherRiskLimit(7.0);

        TransportAsset truck = new TransportAsset();
        truck.setAssetCode("TRUCK-01");
        truck.setCategory("ALL_TERRAIN_RESCUE_TRUCK");
        truck.setStatus("AVAILABLE");
        truck.setMaxPayloadKg(8000.0);
        truck.setMaxRangeKm(800.0);
        truck.setWeatherRiskLimit(8.5);

        List<TransportAsset> fleet = Arrays.asList(heli, truck);

        // Weather is 7.5 (Exceeds Heli limit of 7.0, but fits Truck limit of 8.5)
        WeatherValidationService.SafetyEvaluation eval = weatherService.evaluateAndFilterWeather(
                heli, 7.5, 3000.0, 150.0, fleet
        );

        assertTrue(eval.isAircraftGrounded());
        assertTrue(eval.isSafe()); // Safe because truck alternative was found
        assertNotNull(eval.getReassignedAsset());
        assertEquals("TRUCK-01", eval.getReassignedAsset().getAssetCode());
        assertTrue(eval.getReassignmentNotes().contains("Reassigned safely to All-Terrain Rescue Truck"));
    }

    @Test
    @DisplayName("Should reject asset when payload exceeds capacity")
    void testPayloadOvercapacityRejection() {
        DisasterZone zone = new DisasterZone();
        zone.setFlightDistanceKm(100.0);
        zone.setWeatherSeverityIndex(3.0);

        ReliefCargo heavyCargo = new ReliefCargo();
        heavyCargo.setTotalPayloadKg(9000.0); // 9000 kg

        TransportAsset asset = new TransportAsset();
        asset.setStatus("AVAILABLE");
        asset.setMaxPayloadKg(5000.0); // Only 5000 kg capacity
        asset.setMaxRangeKm(500.0);
        asset.setWeatherRiskLimit(7.0);

        Optional<TransportScoringService.AssetScoringCandidate> chosen =
                scoringService.selectBestFeasibleAsset(zone, heavyCargo, List.of(asset));

        assertTrue(chosen.isEmpty(), "Should not choose asset if payload exceeds max capacity");
    }
}

