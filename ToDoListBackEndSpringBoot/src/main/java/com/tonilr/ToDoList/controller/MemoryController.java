package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.service.MemoryMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryMonitorService memoryMonitorService;

    /**
     * Obtiene el estado actual de la memoria
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMemoryStatus() {
        return ResponseEntity.ok(memoryMonitorService.getMemoryStatus());
    }

    /**
     * Fuerza la ejecución del Garbage Collector
     */
    @PostMapping("/gc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> forceGarbageCollection() {
        memoryMonitorService.forceGarbageCollection();
        return ResponseEntity.ok(Map.of(
            "message", "Garbage Collector ejecutado",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }

    /**
     * Obtiene recomendaciones de optimización
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getOptimizationRecommendations() {
        return ResponseEntity.ok(memoryMonitorService.getOptimizationRecommendations());
    }

    /**
     * Verifica si la memoria está en niveles críticos
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> isMemoryCritical() {
        boolean isCritical = memoryMonitorService.isMemoryCritical();
        return ResponseEntity.ok(Map.of(
            "critical", isCritical,
            "status", isCritical ? "🚨 CRÍTICO" : "✅ NORMAL",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
