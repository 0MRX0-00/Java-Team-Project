package com.disasterrelief;

import com.disasterrelief.dispatch.DispatchExecutor;
import com.disasterrelief.dto.DispatchResultDto;
import com.disasterrelief.model.TransportAsset;
import com.disasterrelief.repository.TransportAssetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Multithreaded Concurrent Dispatch & Safe Asset Allocation Tests")
class MultithreadedDispatchTest {

    @Autowired
    private DispatchExecutor dispatchExecutor;

    @Autowired
    private TransportAssetRepository assetRepository;

    @Test
    @DisplayName("Should execute 5+ concurrent requests without double-allocating any single asset")
    void testConcurrentAssetAllocationSafety() throws Exception {
        // We submit concurrent dispatch requests for multiple disaster zones simultaneously
        int threadCount = 6;
        ExecutorService concurrentExecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<DispatchResultDto>> futures = new ArrayList<>();

        // Target zones from test seed: Zone 1, 3, 4, 6
        Long[] targetZoneIds = { 1L, 3L, 4L, 6L, 1L, 3L };

        for (int i = 0; i < threadCount; i++) {
            final Long zoneId = targetZoneIds[i % targetZoneIds.length];
            futures.add(concurrentExecutor.submit(() -> {
                readyLatch.countDown();
                startLatch.await(); // Synchronize thread release to maximize contention
                return dispatchExecutor.processDispatch(zoneId);
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire all 6 threads simultaneously!

        List<DispatchResultDto> results = new ArrayList<>();
        for (Future<DispatchResultDto> f : futures) {
            results.add(f.get(10, TimeUnit.SECONDS));
        }

        concurrentExecutor.shutdown();

        // Verify that all threads completed their execution cycle
        assertEquals(threadCount, results.size());

        // Extract assigned asset codes for successfully dispatched missions
        List<String> assignedAssets = results.stream()
                .filter(r -> "DISPATCHED".equalsIgnoreCase(r.getStatus()))
                .map(DispatchResultDto::getAssignedAssetCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // CRITICAL INVARIANT: No asset can be double-allocated concurrently!
        Set<String> uniqueAssets = Set.copyOf(assignedAssets);
        assertEquals(assignedAssets.size(), uniqueAssets.size(),
                "Race condition failure! Duplicate asset allocation detected: " + assignedAssets);

        // Assets that were successfully allocated must now be marked as OCCUPIED in database
        for (String assetCode : uniqueAssets) {
            TransportAsset asset = assetRepository.findAll().stream()
                    .filter(a -> a.getAssetCode().equals(assetCode))
                    .findFirst()
                    .orElse(null);
            assertNotNull(asset);
            assertEquals("OCCUPIED", asset.getStatus(),
                    "Asset " + assetCode + " must be OCCUPIED in database after dispatch");
        }
    }
}

