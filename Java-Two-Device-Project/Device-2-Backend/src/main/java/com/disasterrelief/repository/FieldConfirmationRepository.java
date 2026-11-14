package com.disasterrelief.repository;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.model.FieldConfirmation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FieldConfirmationRepository {

    private final ReliefDAO reliefDAO;

    @Autowired
    public FieldConfirmationRepository(ReliefDAO reliefDAO) {
        this.reliefDAO = reliefDAO;
    }

    public FieldConfirmation save(FieldConfirmation fc) {
        return reliefDAO.saveFieldConfirmation(fc);
    }

    public List<FieldConfirmation> findAll() {
        return reliefDAO.findAllFieldConfirmations();
    }

    public Optional<FieldConfirmation> findByMissionId(Long missionId) {
        return reliefDAO.findFieldConfirmationByMissionId(missionId);
    }
}

