package com.disasterrelief.service;

import com.disasterrelief.dispatch.DispatchExecutor;
import com.disasterrelief.dto.DispatchResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchService {

    private final DispatchExecutor dispatchExecutor;

    @Autowired
    public DispatchService(DispatchExecutor dispatchExecutor) {
        this.dispatchExecutor = dispatchExecutor;
    }

    public DispatchResultDto dispatchZone(Long zoneId) {
        return dispatchExecutor.processDispatch(zoneId);
    }

    public List<DispatchResultDto> dispatchBulk(List<Long> zoneIds) {
        return dispatchExecutor.processBulkConcurrentDispatch(zoneIds);
    }
}

