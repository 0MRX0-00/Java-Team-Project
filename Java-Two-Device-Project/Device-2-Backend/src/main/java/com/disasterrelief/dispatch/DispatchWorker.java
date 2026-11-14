package com.disasterrelief.dispatch;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.dto.DispatchResultDto;
import com.disasterrelief.model.*;
import com.disasterrelief.repository.*;
import com.disasterrelief.service.AssetAllocationService;
import com.disasterrelief.service.TransportScoringService;
import com.disasterrelief.service.UrgencyCalculationService;
import com.disasterrelief.service.WeatherValidationService;
import com.disasterrelief.utility.ZoneCodeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * DispatchWorker implements concurrent disaster dispatch scheduling.
 * Executes the mandatory 12-step dispatch lifecycle in a thread-safe manner.
 */
public class DispatchWorker implements Callable<DispatchResultDto>, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DispatchWorker.class);

    private final Long disasterZoneId;
    private final String requestId;

    private final DisasterZoneRepository zoneRepository;
    private final CargoRepository cargoRepository;
    private final TransportAssetRepository assetRepository;
    private final DispatchMissionRepository missionRepository;
    private final ReliefDAO reliefDAO;
    private final UrgencyCalculationService urgencyService;
    private final WeatherValidationService weatherService;
    private final TransportScoringService scoringService;
    private final AssetAllocationService assetAllocationService;

    public DispatchWorker(Long disasterZoneId,
                          String requestId,
                          DisasterZoneRepository zoneRepository,
                          CargoRepository cargoRepository,
                          TransportAssetRepository assetRepository,
                          DispatchMissionRepository missionRepository,
                          ReliefDAO reliefDAO,
                          UrgencyCalculationService urgencyService,
                          WeatherValidationService weatherService,
                          TransportScoringService scoringService,
                          AssetAllocationService assetAllocationService) {
        this.disasterZoneId = disasterZoneId;
        this.requestId = requestId != null ? requestId : "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.zoneRepository = zoneRepository;
        this.cargoRepository = cargoRepository;
        this.assetRepository = assetRepository;
        this.missionRepository = missionRepository;
        this.reliefDAO = reliefDAO;
        this.urgencyService = urgencyService;
        this.weatherService = weatherService;
        this.scoringService = scoringService;
        this.assetAllocationService = assetAllocationService;
    }

    @Override
    public void run() {
        call();
    }

    @Override
    public DispatchResultDto call() {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] DispatchWorker started for Request ID [{}] targeting Zone ID [{}]",
                threadName, requestId, disasterZoneId);

        DispatchResultDto result = new DispatchResultDto();
        String missionCode = "MSN-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        result.setMissionCode(missionCode);

        // Step 1: Receive incoming disaster request
        logAction(null, threadName, requestId, "INFO", "RECEIVE_REQUEST",
                "Received dispatch request for zone ID: " + disasterZoneId);

        // Step 2: Validate the disaster zone
        Optional<DisasterZone> zoneOpt = zoneRepository.findById(disasterZoneId);
        if (zoneOpt.isEmpty()) {
            String err = "Disaster Zone not found for ID: " + disasterZoneId;
            logAction(null, threadName, requestId, "ERROR", "VALIDATE_ZONE", err);
            result.setStatus("ABORTED");
            result.setExecutionMessage(err);
            return result;
        }

        DisasterZone zone = zoneOpt.get();
        result.setZoneCode(zone.getZoneCode());
        result.setZoneName(zone.getZoneName());

        if (!ZoneCodeValidator.isValid(zone.getZoneCode())) {
            String err = "Zone code regex validation failed: " + zone.getZoneCode();
            logAction(null, threadName, requestId, "ERROR", "VALIDATE_ZONE", err);
            result.setStatus("ABORTED");
            result.setExecutionMessage(err);
            return result;
        }

        // Retrieve associated cargo if present
        Optional<ReliefCargo> cargoOpt = cargoRepository.findByZoneId(disasterZoneId);
        ReliefCargo cargo = cargoOpt.orElse(null);

        // Step 3: Calculate DDUS
        double ddus = urgencyService.calculateDDUS(zone.getMedicalUrgencyScore(), zone.getAffectedPopulation(), zone.getTerrainRisk());
        result.setDdusScore(ddus);

        // Step 4: Determine Priority Classification
        String priority = urgencyService.determinePriorityLevel(ddus);
        result.setPriorityLevel(priority);

        logAction(null, threadName, requestId, "INFO", "CALCULATE_DDUS",
                String.format("Calculated DDUS: %.1f -> Classified as %s", ddus, priority));

        if ("MISSION_ABORTED".equalsIgnoreCase(priority)) {
            DispatchMission abortedMission = new DispatchMission();
            abortedMission.setMissionCode(missionCode);
            abortedMission.setDisasterZoneId(zone.getId());
            abortedMission.setDdusScore(ddus);
            abortedMission.setPriorityLevel(priority);
            abortedMission.setWeatherValidationPassed(false);
            abortedMission.setRejectionReason("DDUS score " + ddus + " below minimum operational threshold (60.0)");
            abortedMission.setStatus("ABORTED");
            DispatchMission saved = missionRepository.save(abortedMission);

            result.setMissionId(saved.getId());
            result.setStatus("ABORTED");
            result.setRejectionReason(abortedMission.getRejectionReason());
            result.setExecutionMessage("Mission aborted due to low urgency score");

            logAction(saved.getId(), threadName, requestId, "WARN", "MISSION_ABORTED",
                    "Mission aborted: DDUS score " + ddus + " is below 60.0");
            return result;
        }

        // Step 5 & 6: Evaluate transport suitability and match an available asset
        List<TransportAsset> availableAssets = assetRepository.findAvailable();
        Optional<TransportScoringService.AssetScoringCandidate> bestCandidateOpt =
                scoringService.selectBestFeasibleAsset(zone, cargo, availableAssets);

        DispatchMission mission = new DispatchMission();
        mission.setMissionCode(missionCode);
        mission.setDisasterZoneId(zone.getId());
        if (cargo != null) mission.setReliefCargoId(cargo.getId());
        mission.setDdusScore(ddus);
        mission.setPriorityLevel(priority);

        if (bestCandidateOpt.isEmpty()) {
            // Check weather hazard on zone
            boolean severeWeather = zone.getWeatherSeverityIndex() > 7.0;
            mission.setWeatherValidationPassed(!severeWeather);
            mission.setStatus(severeWeather ? "FLIGHT_GROUNDED" : "ABORTED");
            String reason = severeWeather
                    ? String.format("Zone weather index %.1f exceeds aircraft ceilings; no viable ground alternative", zone.getWeatherSeverityIndex())
                    : "No suitable transport asset available matching payload and range constraints";
            mission.setRejectionReason(reason);

            DispatchMission saved = missionRepository.save(mission);
            result.setMissionId(saved.getId());
            result.setStatus(mission.getStatus());
            result.setRejectionReason(reason);
            result.setExecutionMessage(reason);

            logAction(saved.getId(), threadName, requestId, "WARN", "ASSET_UNAVAILABLE", reason);
            return result;
        }

        TransportScoringService.AssetScoringCandidate candidate = bestCandidateOpt.get();
        TransportAsset candidateAsset = candidate.getAsset();

        // Step 5 Check Weather Conditions & Safety Filter
        double payloadKg = cargo != null ? cargo.getTotalPayloadKg() : 500.0;
        WeatherValidationService.SafetyEvaluation safety = weatherService.evaluateAndFilterWeather(
                candidateAsset,
                zone.getWeatherSeverityIndex(),
                payloadKg,
                zone.getFlightDistanceKm(),
                availableAssets
        );

        TransportAsset finalAsset = candidateAsset;
        double finalSuitabilityScore = candidate.getScore();

        if (safety.isAircraftGrounded()) {
            if (safety.getReassignedAsset() != null) {
                // Reassigned to Ground Rescue Truck!
                finalAsset = safety.getReassignedAsset();
                finalSuitabilityScore = scoringService.scoreTruck(zone, cargo, finalAsset);
                mission.setRejectionReason(safety.getReassignmentNotes());
                mission.setWeatherValidationPassed(true);
                logAction(null, threadName, requestId, "INFO", "WEATHER_REASSIGNMENT", safety.getReassignmentNotes());
            } else {
                // Grounded with no alternative
                mission.setTransportCategory(candidateAsset.getCategory());
                mission.setWeatherValidationPassed(false);
                mission.setRejectionReason(safety.getRejectionReason());
                mission.setStatus("FLIGHT_GROUNDED");
                DispatchMission saved = missionRepository.save(mission);

                result.setMissionId(saved.getId());
                result.setTransportCategory(candidateAsset.getCategory());
                result.setStatus("FLIGHT_GROUNDED");
                result.setRejectionReason(safety.getRejectionReason());
                result.setExecutionMessage("Aircraft grounded due to adverse weather: " + safety.getRejectionReason());

                logAction(saved.getId(), threadName, requestId, "WARN", "FLIGHT_GROUNDED", safety.getRejectionReason());
                return result;
            }
        } else {
            mission.setWeatherValidationPassed(true);
        }

        // Step 7 & 8: Safely Reserve Asset (atomic status lock)
        boolean allocated = assetAllocationService.tryAllocateAsset(finalAsset.getId(), missionCode);
        if (!allocated) {
            String conflictMsg = "Asset " + finalAsset.getAssetCode() + " was claimed by another concurrent thread";
            mission.setStatus("PENDING");
            mission.setRejectionReason(conflictMsg);
            DispatchMission saved = missionRepository.save(mission);

            result.setMissionId(saved.getId());
            result.setStatus("PENDING");
            result.setRejectionReason(conflictMsg);
            result.setExecutionMessage(conflictMsg);

            logAction(saved.getId(), threadName, requestId, "WARN", "ALLOCATION_CONTENTION", conflictMsg);
            return result;
        }

        // Step 9 & 10: Create dispatch mission and persist results
        mission.setTransportAssetId(finalAsset.getId());
        mission.setTransportCategory(finalAsset.getCategory());
        mission.setTransportSuitabilityScore(finalSuitabilityScore);
        mission.setStatus("DISPATCHED");
        mission.setDispatchedAt(LocalDateTime.now());

        // Approximate transit duration: (Distance / speed)
        // Heli: 200 km/h, Truck: 60 km/h, Drone: 100 km/h
        int speedKmh = "HEAVY_CARGO_HELICOPTER".equalsIgnoreCase(finalAsset.getCategory()) ? 200 :
                       "ALL_TERRAIN_RESCUE_TRUCK".equalsIgnoreCase(finalAsset.getCategory()) ? 60 : 100;
        int durationMin = Math.max(15, (int) Math.round((zone.getFlightDistanceKm() / speedKmh) * 60.0));
        mission.setEstimatedDurationMin(durationMin);

        DispatchMission saved = missionRepository.save(mission);

        // Populate Result DTO
        result.setMissionId(saved.getId());
        result.setTransportCategory(finalAsset.getCategory());
        result.setAssignedAssetCode(finalAsset.getAssetCode());
        result.setAssignedAssetName(finalAsset.getAssetName());
        result.setTransportSuitabilityScore(finalSuitabilityScore);
        result.setWeatherValidationPassed(mission.isWeatherValidationPassed());
        result.setRejectionReason(mission.getRejectionReason());
        result.setStatus("DISPATCHED");
        result.setExecutionMessage(String.format("Mission [%s] successfully scheduled and dispatched with %s",
                missionCode, finalAsset.getAssetName()));

        // Step 11: Generate dispatch logs
        logAction(saved.getId(), threadName, requestId, "INFO", "MISSION_DISPATCHED",
                String.format("Mission [%s] dispatched to [%s]. Assigned: %s (%s). Score: %.1f",
                        missionCode, zone.getZoneCode(), finalAsset.getAssetCode(), finalAsset.getCategory(), finalSuitabilityScore));

        logger.info("[{}] DispatchWorker successfully completed mission [{}] for Request [{}]",
                threadName, missionCode, requestId);

        return result;
    }

    private void logAction(Long missionId, String threadName, String reqId, String level, String action, String message) {
        try {
            DispatchLog dl = new DispatchLog(missionId, threadName, reqId, level, action, message);
            reliefDAO.insertLog(dl);
        } catch (Exception e) {
            logger.warn("Failed to insert dispatch log: {}", e.getMessage());
        }
    }
}

