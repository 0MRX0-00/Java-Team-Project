package com.disasterrelief.dispatch;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.dto.DispatchResultDto;
import com.disasterrelief.repository.*;
import com.disasterrelief.service.AssetAllocationService;
import com.disasterrelief.service.TransportScoringService;
import com.disasterrelief.service.UrgencyCalculationService;
import com.disasterrelief.service.WeatherValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * DispatchExecutor coordinates single and concurrent bulk dispatch executions
 * across the background worker thread pool.
 */
@Component
public class DispatchExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DispatchExecutor.class);

    private final ThreadPoolManager threadPoolManager;
    private final DisasterZoneRepository zoneRepository;
    private final CargoRepository cargoRepository;
    private final TransportAssetRepository assetRepository;
    private final DispatchMissionRepository missionRepository;
    private final ReliefDAO reliefDAO;
    private final UrgencyCalculationService urgencyService;
    private final WeatherValidationService weatherService;
    private final TransportScoringService scoringService;
    private final AssetAllocationService assetAllocationService;

    @Autowired
    public DispatchExecutor(ThreadPoolManager threadPoolManager,
                            DisasterZoneRepository zoneRepository,
                            CargoRepository cargoRepository,
                            TransportAssetRepository assetRepository,
                            DispatchMissionRepository missionRepository,
                            ReliefDAO reliefDAO,
                            UrgencyCalculationService urgencyService,
                            WeatherValidationService weatherService,
                            TransportScoringService scoringService,
                            AssetAllocationService assetAllocationService) {
        this.threadPoolManager = threadPoolManager;
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

    /**
     * Dispatches a single disaster zone request synchronously using a background worker thread.
     */
    public DispatchResultDto processDispatch(Long zoneId) {
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        DispatchWorker worker = createWorker(zoneId, requestId);
        Future<DispatchResultDto> future = threadPoolManager.submit(worker, requestId);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error executing dispatch worker: {}", e.getMessage());
            DispatchResultDto err = new DispatchResultDto();
            err.setStatus("ERROR");
            err.setExecutionMessage("Dispatch execution failed: " + e.getMessage());
            return err;
        }
    }

    /**
     * Executes bulk concurrent dispatches across multiple zones simultaneously,
     * demonstrating race-condition resilience and multi-threading capabilities.
     */
    public List<DispatchResultDto> processBulkConcurrentDispatch(List<Long> zoneIds) {
        logger.info("Submitting {} concurrent dispatch requests to ThreadPoolManager", zoneIds.size());
        List<Future<DispatchResultDto>> futures = new ArrayList<>();

        for (Long zoneId : zoneIds) {
            String requestId = "BULK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            DispatchWorker worker = createWorker(zoneId, requestId);
            futures.add(threadPoolManager.submit(worker, requestId));
        }

        List<DispatchResultDto> results = new ArrayList<>();
        for (Future<DispatchResultDto> f : futures) {
            try {
                results.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Bulk worker error: {}", e.getMessage());
                DispatchResultDto err = new DispatchResultDto();
                err.setStatus("ERROR");
                err.setExecutionMessage(e.getMessage());
                results.add(err);
            }
        }
        return results;
    }

    private DispatchWorker createWorker(Long zoneId, String requestId) {
        return new DispatchWorker(
                zoneId, requestId, zoneRepository, cargoRepository, assetRepository,
                missionRepository, reliefDAO, urgencyService, weatherService,
                scoringService, assetAllocationService
        );
    }
}

