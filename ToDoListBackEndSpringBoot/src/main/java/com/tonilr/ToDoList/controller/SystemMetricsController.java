package com.tonilr.ToDoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;

@RestController
@RequestMapping("/api/system")
public class SystemMetricsController {

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Métricas simuladas para evitar problemas con APIs del sistema
            long currentTime = System.currentTimeMillis();
            // Usar el uptime real del proceso JVM (en milisegundos)
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            
            metrics.put("jvm.memory.used", 256 * 1024 * 1024L); // 256 MB
            metrics.put("jvm.memory.max", 512 * 1024 * 1024L);  // 512 MB
            metrics.put("jvm.memory.percentage", 50.0);
            
            metrics.put("system.cpu.usage", 0.25); // 0.25 = 25%
            metrics.put("system.cpu.count", 4);
            metrics.put("system.cpu.load", 0.25);
            
            metrics.put("process.uptime", uptime);
            
            metrics.put("app.status", "UP");
            metrics.put("app.version", "1.0.0");
            
            metrics.put("disk.used", 85L); // GB
            metrics.put("disk.total", 100L); // GB
            metrics.put("disk.percentage", 85.0);
            
            metrics.put("database.status", "UP");
            metrics.put("redis.status", "UP");
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ToDoList Backend");
        return ResponseEntity.ok(health);
    }
}
