package com.tonilr.ToDoList.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Servicio para proporcionar métricas del sistema en tiempo real.
 * Actualiza las métricas cada 5 segundos por defecto.
 */
@Service
@Slf4j
public class RealTimeMetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    private final AtomicReference<Map<String, Object>> currentMetrics = new AtomicReference<>();
    private final AtomicReference<Map<String, Object>> previousMetrics = new AtomicReference<>();
    
    // Cache de métricas para evitar cálculos repetitivos
    private final ConcurrentHashMap<String, Object> metricsCache = new ConcurrentHashMap<>();
    
    // Timestamp de la última actualización
    private volatile long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 5000; // 5 segundos

    /**
     * Actualiza las métricas del sistema cada 5 segundos.
     */
    @Scheduled(fixedRate = 5000)
    public void updateMetrics() {
        try {
            Map<String, Object> newMetrics = collectSystemMetrics();
            
            // Mover métricas actuales a anteriores
            previousMetrics.set(currentMetrics.get());
            currentMetrics.set(newMetrics);
            
            // Actualizar cache
            metricsCache.putAll(newMetrics);
            lastUpdateTime = System.currentTimeMillis();
            
            log.debug("Métricas del sistema actualizadas: {}", newMetrics.get("timestamp"));
            
        } catch (Exception e) {
            log.error("Error actualizando métricas del sistema: {}", e.getMessage());
        }
    }

    /**
     * Obtiene las métricas actuales del sistema.
     * @return Métricas del sistema
     */
    public Map<String, Object> getCurrentMetrics() {
        Map<String, Object> metrics = currentMetrics.get();
        if (metrics == null || isMetricsStale()) {
            // Si las métricas están desactualizadas, actualizarlas inmediatamente
            updateMetrics();
            metrics = currentMetrics.get();
        }
        return metrics != null ? new HashMap<>(metrics) : new HashMap<>();
    }

    /**
     * Obtiene métricas con cálculo de tendencias.
     * @return Métricas con información de tendencias
     */
    public Map<String, Object> getMetricsWithTrends() {
        Map<String, Object> current = currentMetrics.get();
        Map<String, Object> previous = previousMetrics.get();
        
        if (current == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> metricsWithTrends = new HashMap<>(current);
        
        if (previous != null) {
            // Calcular tendencias
            calculateTrends(metricsWithTrends, current, previous);
        }
        
        return metricsWithTrends;
    }

    /**
     * Verifica si las métricas están desactualizadas.
     * @return true si las métricas están desactualizadas
     */
    private boolean isMetricsStale() {
        return System.currentTimeMillis() - lastUpdateTime > UPDATE_INTERVAL;
    }

    /**
     * Recolecta métricas del sistema.
     * @return Métricas del sistema
     */
    private Map<String, Object> collectSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            long currentTime = System.currentTimeMillis();
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            
            // Métricas de memoria JVM
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryPercentage = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
            
            metrics.put("jvm.memory.used", usedMemory);
            metrics.put("jvm.memory.max", maxMemory);
            metrics.put("jvm.memory.percentage", Math.round(memoryPercentage * 100.0) / 100.0);
            
            // Métricas de CPU
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuLoad = osBean.getSystemLoadAverage();
            int cpuCores = osBean.getAvailableProcessors();
            
            // Calcular uso de CPU
            double cpuUsage = Math.min(100.0, Math.max(0.0, (cpuLoad / cpuCores) * 100));
            
            metrics.put("system.cpu.usage", Math.round(cpuUsage * 100.0) / 100.0);
            metrics.put("system.cpu.count", cpuCores);
            metrics.put("system.cpu.load", Math.round(cpuLoad * 100.0) / 100.0);
            
            // Métricas de proceso
            metrics.put("process.uptime", uptime);
            
            // Estado de la aplicación
            metrics.put("app.status", "UP");
            metrics.put("app.version", "1.0.0");
            
            // Métricas de disco
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskPercentage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            
            metrics.put("disk.used", Math.round(usedSpace / (1024.0 * 1024.0 * 1024.0) * 100.0) / 100.0);
            metrics.put("disk.total", Math.round(totalSpace / (1024.0 * 1024.0 * 1024.0) * 100.0) / 100.0);
            metrics.put("disk.percentage", Math.round(diskPercentage * 100.0) / 100.0);
            
            // Métricas de threads
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();
            
            metrics.put("jvm.threads.count", threadCount);
            metrics.put("jvm.threads.peak", peakThreadCount);
            
            // Estado de servicios
            metrics.put("database.status", "UP");
            metrics.put("redis.status", "UP");
            
            // Timestamp
            metrics.put("timestamp", currentTime);
            
            // Métricas adicionales de rendimiento
            metrics.put("jvm.memory.heap.used", memoryBean.getHeapMemoryUsage().getUsed());
            metrics.put("jvm.memory.heap.committed", memoryBean.getHeapMemoryUsage().getCommitted());
            metrics.put("jvm.memory.nonheap.used", memoryBean.getNonHeapMemoryUsage().getUsed());
            
        } catch (Exception e) {
            log.error("Error recolectando métricas del sistema: {}", e.getMessage());
            metrics.put("error", "Error recolectando métricas: " + e.getMessage());
        }
        
        return metrics;
    }

    /**
     * Calcula tendencias entre métricas actuales y anteriores.
     * @param metricsWithTrends Métricas con tendencias
     * @param current Métricas actuales
     * @param previous Métricas anteriores
     */
    private void calculateTrends(Map<String, Object> metricsWithTrends, Map<String, Object> current, Map<String, Object> previous) {
        try {
            // Calcular tendencia de memoria
            if (current.containsKey("jvm.memory.percentage") && previous.containsKey("jvm.memory.percentage")) {
                double currentMem = (Double) current.get("jvm.memory.percentage");
                double previousMem = (Double) previous.get("jvm.memory.percentage");
                double memoryTrend = currentMem - previousMem;
                
                metricsWithTrends.put("jvm.memory.trend", Math.round(memoryTrend * 100.0) / 100.0);
                metricsWithTrends.put("jvm.memory.trend.direction", memoryTrend > 0 ? "UP" : memoryTrend < 0 ? "DOWN" : "STABLE");
            }
            
            // Calcular tendencia de CPU
            if (current.containsKey("system.cpu.usage") && previous.containsKey("system.cpu.usage")) {
                double currentCpu = (Double) current.get("system.cpu.usage");
                double previousCpu = (Double) previous.get("system.cpu.usage");
                double cpuTrend = currentCpu - previousCpu;
                
                metricsWithTrends.put("system.cpu.trend", Math.round(cpuTrend * 100.0) / 100.0);
                metricsWithTrends.put("system.cpu.trend.direction", cpuTrend > 0 ? "UP" : cpuTrend < 0 ? "DOWN" : "STABLE");
            }
            
            // Calcular tendencia de threads
            if (current.containsKey("jvm.threads.count") && previous.containsKey("jvm.threads.count")) {
                int currentThreads = (Integer) current.get("jvm.threads.count");
                int previousThreads = (Integer) previous.get("jvm.threads.count");
                int threadTrend = currentThreads - previousThreads;
                
                metricsWithTrends.put("jvm.threads.trend", threadTrend);
                metricsWithTrends.put("jvm.threads.trend.direction", threadTrend > 0 ? "UP" : threadTrend < 0 ? "DOWN" : "STABLE");
            }
            
        } catch (Exception e) {
            log.warn("Error calculando tendencias: {}", e.getMessage());
        }
    }

    /**
     * Fuerza una actualización inmediata de las métricas.
     */
    public void forceUpdate() {
        updateMetrics();
    }

    /**
     * Obtiene el tiempo de la última actualización.
     * @return Timestamp de la última actualización
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Verifica si el servicio está funcionando correctamente.
     * @return true si el servicio está funcionando
     */
    public boolean isHealthy() {
        return currentMetrics.get() != null && !isMetricsStale();
    }
}
