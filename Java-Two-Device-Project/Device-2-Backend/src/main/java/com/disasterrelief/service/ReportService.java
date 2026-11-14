package com.disasterrelief.service;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.dto.DashboardStatsDto;
import com.disasterrelief.dto.ReliefCoverageDto;
import com.disasterrelief.model.DispatchLog;
import com.disasterrelief.model.DispatchMission;
import com.disasterrelief.model.FieldConfirmation;
import com.disasterrelief.repository.DispatchMissionRepository;
import com.disasterrelief.repository.FieldConfirmationRepository;
import com.disasterrelief.utility.ManifestWriter;
import com.disasterrelief.utility.ReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ReportService {

    private final DispatchMissionRepository missionRepository;
    private final FieldConfirmationRepository confirmationRepository;
    private final ReliefDAO reliefDAO;
    private final CoverageService coverageService;
    private final ManifestWriter manifestWriter = new ManifestWriter();

    @Autowired
    public ReportService(DispatchMissionRepository missionRepository,
                         FieldConfirmationRepository confirmationRepository,
                         ReliefDAO reliefDAO,
                         CoverageService coverageService) {
        this.missionRepository = missionRepository;
        this.confirmationRepository = confirmationRepository;
        this.reliefDAO = reliefDAO;
        this.coverageService = coverageService;
    }

    public String generateAndReadFlightManifests() throws IOException {
        List<DispatchMission> missions = missionRepository.findAll();
        Path p = manifestWriter.writeEmergencyFlightManifests(missions);
        return Files.readString(p);
    }

    public String generateAndReadSupplyDropLogs() throws IOException {
        List<FieldConfirmation> confirmations = confirmationRepository.findAll();
        Path p = manifestWriter.writeSupplyDropLogs(confirmations);
        return Files.readString(p);
    }

    public String generateAndReadSummary(DashboardStatsDto stats) throws IOException {
        List<DispatchMission> missions = missionRepository.findAll();
        List<ReliefCoverageDto> coverages = coverageService.getZoneCoverages();
        String summary = ReportGenerator.generateComprehensiveSummary(stats, missions, coverages);
        Path p = manifestWriter.writeDispatchSummary(summary);
        return Files.readString(p);
    }

    public List<DispatchLog> getRecentLogs(int limit) {
        return reliefDAO.getRecentLogs(limit);
    }
}

