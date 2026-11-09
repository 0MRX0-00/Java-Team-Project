package com.disasterrelief.dispatch;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPoolManager manages a FixedThreadPool ExecutorService and safe concurrency metrics
 * for the DREMLDS multithreaded dispatch system.
 */
@Component
public class ThreadPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    private final ExecutorService executorService;
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final AtomicInteger completedTaskCount = new AtomicInteger(0);
    private final ConcurrentHashMap<String, String> runningMissions = new ConcurrentHashMap<>();

    public ThreadPoolManager(@Value("${app.dispatch.thread-pool-size:8}") int poolSize) {
        int effectivePoolSize = Math.max(5, poolSize); // Mandatory minimum 5 concurrent workers
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(500);

        AtomicInteger workerNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r);
            t.setName(String.format("DispatchWorker-%d", workerNumber.getAndIncrement()));
            t.setDaemon(true);
            return t;
        };

        this.executorService = new ThreadPoolExecutor(
                effectivePoolSize,
                effectivePoolSize,
                60L, TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        logger.info("ThreadPoolManager initialized with FixedThreadPool size of {} workers", effectivePoolSize);
    }

    public <T> Future<T> submit(Callable<T> task, String requestId) {
        activeTaskCount.incrementAndGet();
        runningMissions.put(requestId, "PROCESSING");
        return executorService.submit(() -> {
            try {
                return task.call();
            } finally {
                activeTaskCount.decrementAndGet();
                completedTaskCount.incrementAndGet();
                runningMissions.remove(requestId);
            }
        });
    }

    public void execute(Runnable task) {
        activeTaskCount.incrementAndGet();
        executorService.execute(() -> {
            try {
                task.run();
            } finally {
                activeTaskCount.decrementAndGet();
                completedTaskCount.incrementAndGet();
            }
        });
    }

    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    public int getCompletedTaskCount() {
        return completedTaskCount.get();
    }

    public ConcurrentHashMap<String, String> getRunningMissions() {
        return runningMissions;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down ThreadPoolManager ExecutorService...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

