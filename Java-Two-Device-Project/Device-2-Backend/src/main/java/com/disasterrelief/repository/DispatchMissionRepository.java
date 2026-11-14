package com.disasterrelief.repository;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.model.DispatchMission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DispatchMissionRepository {

    private final ReliefDAO reliefDAO;

    @Autowired
    public DispatchMissionRepository(ReliefDAO reliefDAO) {
        this.reliefDAO = reliefDAO;
    }

    public DispatchMission save(DispatchMission mission) {
        return reliefDAO.saveMission(mission);
    }

    public List<DispatchMission> findAll() {
        return reliefDAO.findAllMissions();
    }

    public Optional<DispatchMission> findById(Long id) {
        return reliefDAO.findMissionById(id);
    }

    public List<DispatchMission> findByStatus(String status) {
        return reliefDAO.findMissionsByStatus(status);
    }

    public void updateStatus(Long id, String status, LocalDateTime completedAt) {
        reliefDAO.updateMissionStatus(id, status, completedAt);
    }

    public int countByStatus(String status) {
        return reliefDAO.countRows("dispatch_missions", "status = '" + status + "'");
    }
}

