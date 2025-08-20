package com.tonilr.ToDoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de health básico y eficiente.
 * Reemplaza los controladores de métricas pesados para optimizar memoria y costos.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Endpoint de health básico para Railway.
     * Muy ligero y eficiente.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ToDoList Backend");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint de health detallado solo para administradores.
     * Incluye información básica del sistema sin cálculos pesados.
     */
    @GetMapping("/detailed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ToDoList Backend");
        health.put("version", "1.0.0");
        
        // Información básica del sistema (sin cálculos pesados)
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        health.put("memory.total.mb", Math.round(totalMemory / (1024.0 * 1024.0) * 100.0) / 100.0);
        health.put("memory.used.mb", Math.round(usedMemory / (1024.0 * 1024.0) * 100.0) / 100.0);
        health.put("memory.free.mb", Math.round(freeMemory / (1024.0 * 1024.0) * 100.0) / 100.0);
        
        // Uptime básico
        long uptime = System.currentTimeMillis() - System.nanoTime() / 1_000_000;
        health.put("uptime.seconds", Math.round(uptime / 1000.0));
        
        // Información de Java
        health.put("java.version", System.getProperty("java.version"));
        health.put("java.vendor", System.getProperty("java.vendor"));
        
        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint de readiness para Railway.
     * Indica si el servicio está listo para recibir requests.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", System.currentTimeMillis());
        readiness.put("message", "Service is ready to handle requests");
        
        return ResponseEntity.ok(readiness);
    }

    /**
     * Endpoint de liveness para Railway.
     * Indica si el servicio está vivo y funcionando.
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> getLiveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", System.currentTimeMillis());
        liveness.put("message", "Service is alive and running");
        
        return ResponseEntity.ok(liveness);
    }
}
