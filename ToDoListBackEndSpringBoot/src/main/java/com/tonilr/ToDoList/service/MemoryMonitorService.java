package com.tonilr.ToDoList.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MemoryMonitorService {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    /**
     * Obtiene el estado actual de la memoria
     */
    public Map<String, Object> getMemoryStatus() {
        Map<String, Object> status = new HashMap<>();
        
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        // Memoria Heap
        status.put("heapUsed", formatBytes(heapMemory.getUsed()));
        status.put("heapMax", formatBytes(heapMemory.getMax()));
        status.put("heapCommitted", formatBytes(heapMemory.getCommitted()));
        status.put("heapUsagePercent", calculateUsagePercent(heapMemory.getUsed(), heapMemory.getMax()));
        
        // Memoria No-Heap
        status.put("nonHeapUsed", formatBytes(nonHeapMemory.getUsed()));
        status.put("nonHeapCommitted", formatBytes(nonHeapMemory.getCommitted()));
        
        // Estado general
        status.put("memoryPressure", getMemoryPressureLevel(heapMemory));
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }

    /**
     * Fuerza la ejecución del Garbage Collector
     */
    public void forceGarbageCollection() {
        log.info("🔄 Forzando ejecución del Garbage Collector...");
        System.gc();
        log.info("✅ Garbage Collector ejecutado");
    }

    /**
     * Verifica si la memoria está en niveles críticos
     */
    public boolean isMemoryCritical() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        double usagePercent = calculateUsagePercent(heapMemory.getUsed(), heapMemory.getMax());
        return usagePercent > 85.0;
    }

    /**
     * Obtiene recomendaciones de optimización basadas en el uso actual
     */
    public Map<String, String> getOptimizationRecommendations() {
        Map<String, String> recommendations = new HashMap<>();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        double usagePercent = calculateUsagePercent(heapMemory.getUsed(), heapMemory.getMax());
        
        if (usagePercent > 80) {
            recommendations.put("priority", "ALTA");
            recommendations.put("action", "Considerar aumentar heap o optimizar queries");
            recommendations.put("cache", "Reducir tamaño de caché Caffeine");
        } else if (usagePercent > 60) {
            recommendations.put("priority", "MEDIA");
            recommendations.put("action", "Monitorear tendencia de crecimiento");
            recommendations.put("cache", "Revisar configuración de caché");
        } else {
            recommendations.put("priority", "BAJA");
            recommendations.put("action", "Memoria en niveles normales");
            recommendations.put("cache", "Configuración actual es adecuada");
        }
        
        return recommendations;
    }

    /**
     * Log del estado de memoria (para monitoreo continuo)
     */
    public void logMemoryStatus() {
        Map<String, Object> status = getMemoryStatus();
        String pressure = (String) status.get("memoryPressure");
        
        if ("CRÍTICO".equals(pressure)) {
            log.warn("🚨 MEMORIA CRÍTICA: Heap usado: {}%", status.get("heapUsagePercent"));
        } else if ("ALTO".equals(pressure)) {
            log.warn("⚠️ MEMORIA ALTA: Heap usado: {}%", status.get("heapUsagePercent"));
        } else {
            log.info("✅ Memoria OK: Heap usado: {}%", status.get("heapUsagePercent"));
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private double calculateUsagePercent(long used, long max) {
        if (max == -1) return 0.0; // Max no definido
        return (double) used / max * 100.0;
    }

    private String getMemoryPressureLevel(MemoryUsage heapMemory) {
        double usagePercent = calculateUsagePercent(heapMemory.getUsed(), heapMemory.getMax());
        
        if (usagePercent > 85) return "CRÍTICO";
        if (usagePercent > 70) return "ALTO";
        if (usagePercent > 50) return "MEDIO";
        return "NORMAL";
    }
}
