package com.disasterrelief.service;

import com.disasterrelief.dto.CargoIngestionRequest;
import com.disasterrelief.exception.ResourceNotFoundException;
import com.disasterrelief.model.ReliefCargo;
import com.disasterrelief.repository.CargoRepository;
import com.disasterrelief.repository.DisasterZoneRepository;
import com.disasterrelief.utility.InputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final DisasterZoneRepository zoneRepository;

    @Autowired
    public CargoService(CargoRepository cargoRepository, DisasterZoneRepository zoneRepository) {
        this.cargoRepository = cargoRepository;
        this.zoneRepository = zoneRepository;
    }

    public ReliefCargo registerCargo(CargoIngestionRequest req) {
        // Validate zone existence
        zoneRepository.findById(req.getDisasterZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Disaster Zone not found with ID: " + req.getDisasterZoneId()));

        // Validate cargo manifest string format using required Java string operations
        String cleanManifest = InputValidator.cleanText(req.getCargoManifest());
        InputValidator.parseAndValidateManifest(cleanManifest);

        ReliefCargo cargo = new ReliefCargo();
        cargo.setDisasterZoneId(req.getDisasterZoneId());
        cargo.setFoodKitCount(req.getFoodKitCount());
        cargo.setWaterKitCount(req.getWaterKitCount());
        cargo.setPlasmaKitCount(req.getPlasmaKitCount());
        cargo.setTotalPayloadKg(req.getTotalPayloadKg());
        cargo.setRequiredRations(req.getRequiredRations());
        cargo.setDispatchLocation(InputValidator.cleanText(req.getDispatchLocation()));
        cargo.setCargoManifest(cleanManifest);

        return cargoRepository.save(cargo);
    }

    public List<ReliefCargo> getAllCargo() {
        return cargoRepository.findAll();
    }

    public ReliefCargo getCargoById(Long id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relief Cargo not found with ID: " + id));
    }

    public ReliefCargo getCargoByZoneId(Long zoneId) {
        return cargoRepository.findByZoneId(zoneId)
                .orElse(null);
    }
}

