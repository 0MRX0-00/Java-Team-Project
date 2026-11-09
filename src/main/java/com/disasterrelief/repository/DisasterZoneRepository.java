package com.disasterrelief.repository;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.model.DisasterZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DisasterZoneRepository {

    private final ReliefDAO reliefDAO;

    @Autowired
    public DisasterZoneRepository(ReliefDAO reliefDAO) {
        this.reliefDAO = reliefDAO;
    }

    public DisasterZone save(DisasterZone zone) {
        return reliefDAO.saveDisasterZone(zone);
    }

    public List<DisasterZone> findAll() {
        return reliefDAO.findAllDisasterZones();
    }

    public Optional<DisasterZone> findById(Long id) {
        return reliefDAO.findDisasterZoneById(id);
    }

    public Optional<DisasterZone> findByZoneCode(String zoneCode) {
        return reliefDAO.findDisasterZoneByCode(zoneCode);
    }

    public void update(DisasterZone zone) {
        reliefDAO.updateDisasterZone(zone);
    }

    public int count() {
        return reliefDAO.countRows("disaster_zones", null);
    }
}

