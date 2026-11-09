package com.disasterrelief.model;

import java.time.LocalDateTime;

public class DispatchLog {
    private Long id;
    private Long missionId;
    private String threadName;
    private String requestId;
    private String logLevel; // INFO, WARN, ERROR
    private String action;
    private String message;
    private LocalDateTime timestamp;

    public DispatchLog() {}

    public DispatchLog(Long missionId, String threadName, String requestId, String logLevel, String action, String message) {
        this.missionId = missionId;
        this.threadName = threadName;
        this.requestId = requestId;
        this.logLevel = logLevel;
        this.action = action;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

