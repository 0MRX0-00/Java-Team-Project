package com.disasterrelief.repository;

import com.disasterrelief.dao.ReliefDAO;
import com.disasterrelief.model.TransportAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TransportAssetRepository {

    private final ReliefDAO reliefDAO;

    @Autowired
    public TransportAssetRepository(ReliefDAO reliefDAO) {
        this.reliefDAO = reliefDAO;
    }

    public List<TransportAsset> findAll() {
        return reliefDAO.findAllTransportAssets();
    }

    public List<TransportAsset> findAvailable() {
        return reliefDAO.findAvailableTransportAssets();
    }

    public Optional<TransportAsset> findById(Long id) {
        return reliefDAO.findTransportAssetById(id);
    }

    public TransportAsset save(TransportAsset asset) {
        return reliefDAO.saveTransportAsset(asset);
    }

    public void updateStatus(Long id, String status) {
        reliefDAO.updateTransportAssetStatus(id, status);
    }

    public boolean reserveAssetAtomically(Long id) {
        return reliefDAO.reserveAssetAtomically(id);
    }

    public void releaseAsset(Long id) {
        reliefDAO.releaseAsset(id);
    }
}

