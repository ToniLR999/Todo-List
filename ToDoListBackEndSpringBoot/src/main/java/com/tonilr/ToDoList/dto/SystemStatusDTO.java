package com.tonilr.ToDoList.dto;

import java.time.LocalDateTime;

public class SystemStatusDTO {
    private String status;
    private String schedule;
    private LocalDateTime lastUpdate;
    private String version;
    private String environment;
    private long uptimeSeconds;

    public SystemStatusDTO() {
        this.status = "UNKNOWN";
        this.schedule = "N/A";
        this.lastUpdate = LocalDateTime.now();
        this.version = "N/A";
        this.environment = "N/A";
    }

    public SystemStatusDTO(String status, String schedule, String version, String environment) {
        this.status = status;
        this.schedule = schedule;
        this.lastUpdate = LocalDateTime.now();
        this.version = version;
        this.environment = environment;
        this.uptimeSeconds = 0;
    }

    public SystemStatusDTO(String status, String schedule, String version, String environment, long uptimeSeconds) {
        this.status = status;
        this.schedule = schedule;
        this.lastUpdate = LocalDateTime.now();
        this.version = version;
        this.environment = environment;
        this.uptimeSeconds = uptimeSeconds;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }
}
