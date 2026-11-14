package com.disasterrelief.service;

import com.disasterrelief.dto.DeliveryAuditRequest;
import com.disasterrelief.exception.ResourceNotFoundException;
import com.disasterrelief.exception.ValidationException;
import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.model.FieldConfirmation;
import com.disasterrelief.repository.DispatchMissionRepository;
import com.disasterrelief.repository.FieldConfirmationRepository;
import com.disasterrelief.utility.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryAuditService {

    private final FieldConfirmationRepository confirmationRepository;
    private final DispatchMissionRepository missionRepository;
    private final AssetAllocationService assetAllocationService;

    @Autowired
    public DeliveryAuditService(FieldConfirmationRepository confirmationRepository,
                                DispatchMissionRepository missionRepository,
                                AssetAllocationService assetAllocationService) {
        this.confirmationRepository = confirmationRepository;
        this.missionRepository = missionRepository;
        this.assetAllocationService = assetAllocationService;
    }

    @Transactional
    public FieldConfirmation recordConfirmation(Long missionId, DeliveryAuditRequest req) {
        DispatchMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with ID: " + missionId));

        if ("COMPLETED".equalsIgnoreCase(mission.getStatus())) {
            throw new ValidationException("Mission " + mission.getMissionCode() + " has already been audited and confirmed");
        }

        if (confirmationRepository.findByMissionId(missionId).isPresent()) {
            throw new ValidationException("Field confirmation already exists for mission ID: " + missionId);
        }

        FieldConfirmation fc = new FieldConfirmation();
        fc.setMissionId(missionId);
        fc.setFlightTransitDurationMin(req.getFlightTransitDurationMin());
        fc.setDropOffPrecisionMeters(req.getDropOffPrecisionMeters());
        fc.setFieldOfficerName(InputValidator.cleanText(req.getFieldOfficerName()));
        fc.setDeliveredRations(req.getDeliveredRations());
        fc.setDeliveryNotes(InputValidator.cleanText(req.getDeliveryNotes()));
        fc.setDeliveryVerified(req.isDeliveryVerified());
        fc.setFieldConfirmationTimestamp(LocalDateTime.now());

        FieldConfirmation saved = confirmationRepository.save(fc);

        // Update mission status to COMPLETED and record completed_at
        missionRepository.updateStatus(missionId, "COMPLETED", LocalDateTime.now());

        // Release the assigned asset back to AVAILABLE status
        if (mission.getTransportAssetId() != null) {
            assetAllocationService.releaseAsset(mission.getTransportAssetId(), mission.getMissionCode());
        }

        return saved;
    }

    public List<FieldConfirmation> getAllConfirmations() {
        return confirmationRepository.findAll();
    }
}

