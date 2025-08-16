package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.SystemStatusDTO;
import com.tonilr.ToDoList.service.AdminService;
import com.tonilr.ToDoList.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/app-status")
@CrossOrigin(origins = "*")
public class AppStatusController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAppStatus() {
        try {
            SystemStatusDTO systemInfo = adminService.getSystemInfo();
            
            Map<String, Object> status = Map.of(
                "status", systemInfo.getStatus(),
                "schedule", systemInfo.getSchedule(),
                "version", systemInfo.getVersion(),
                "environment", systemInfo.getEnvironment(),
                "uptime", systemInfo.getUptimeSeconds(),
                "nextStart", scheduleService.getNextStartTime(),
                "scheduleStatus", scheduleService.getScheduleStatus(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "UNKNOWN",
                "schedule", "N/A",
                "version", "N/A",
                "environment", "N/A",
                "uptime", 0L,
                "nextStart", "N/A",
                "scheduleStatus", "UNKNOWN",
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
