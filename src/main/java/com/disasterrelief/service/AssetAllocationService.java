package com.disasterrelief.service;

import com.disasterrelief.model.TransportAsset;
import com.disasterrelief.repository.TransportAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * AssetAllocationService manages thread-safe asset reservations and status transitions.
 * Prevents race conditions and double-dispatch in concurrent environments.
 */
@Service
public class AssetAllocationService {

    private static final Logger logger = LoggerFactory.getLogger(AssetAllocationService.class);
    private final TransportAssetRepository assetRepository;

    @Autowired
    public AssetAllocationService(TransportAssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    /**
     * Attempts atomic reservation of a transport asset.
     * @return true if reservation successfully acquired, false if asset was already taken by a concurrent worker.
     */
    public synchronized boolean tryAllocateAsset(Long assetId, String missionCode) {
        if (assetId == null) return false;

        boolean reserved = assetRepository.reserveAssetAtomically(assetId);
        if (reserved) {
            logger.info("Successfully reserved asset ID [{}] for mission [{}]", assetId, missionCode);
            return true;
        } else {
            logger.warn("Contention detected: asset ID [{}] could not be reserved for mission [{}] (Already occupied)",
                    assetId, missionCode);
            return false;
        }
    }

    /**
     * Releases an asset back to AVAILABLE status upon mission completion or rollback.
     */
    public synchronized void releaseAsset(Long assetId, String missionCode) {
        if (assetId == null) return;
        assetRepository.releaseAsset(assetId);
        logger.info("Released asset ID [{}] back to AVAILABLE after mission [{}]", assetId, missionCode);
    }

    public Optional<TransportAsset> getAssetById(Long assetId) {
        return assetRepository.findById(assetId);
    }
}

