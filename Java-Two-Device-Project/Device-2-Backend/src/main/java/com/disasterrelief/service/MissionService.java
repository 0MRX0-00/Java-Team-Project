package com.disasterrelief.service;

import com.disasterrelief.exception.ResourceNotFoundException;
import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.repository.DispatchMissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissionService {

    private final DispatchMissionRepository missionRepository;

    @Autowired
    public MissionService(DispatchMissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public List<DispatchMission> getAllMissions() {
        return missionRepository.findAll();
    }

    public DispatchMission getMissionById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch Mission not found with ID: " + id));
    }

    public List<DispatchMission> getMissionsByStatus(String status) {
        return missionRepository.findByStatus(status.toUpperCase());
    }
}

