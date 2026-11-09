package com.disasterrelief.repository;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.model.ReliefCargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CargoRepository {

    private final ReliefDAO reliefDAO;

    @Autowired
    public CargoRepository(ReliefDAO reliefDAO) {
        this.reliefDAO = reliefDAO;
    }

    public ReliefCargo save(ReliefCargo cargo) {
        return reliefDAO.saveCargo(cargo);
    }

    public List<ReliefCargo> findAll() {
        return reliefDAO.findAllCargo();
    }

    public Optional<ReliefCargo> findById(Long id) {
        return reliefDAO.findCargoById(id);
    }

    public Optional<ReliefCargo> findByZoneId(Long zoneId) {
        return reliefDAO.findCargoByZoneId(zoneId);
    }
}

