package com.tonilr.ToDoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.tonilr.ToDoList.service.RealTimeMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ClassLoadingMXBean;
import java.util.List;

/**
 * Controlador avanzado para métricas del sistema.
 * Proporciona endpoints adicionales para monitoreo detallado.
 */
@RestController
@RequestMapping("/api/metrics")
@SecurityRequirement(name = "bearerAuth")
public class AdvancedMetricsController {

    @Autowired
    private RealTimeMetricsService realTimeMetricsService;

    /**
     * Fuerza una actualización inmediata de las métricas.
     * @return Confirmación de la actualización
     */
    @Operation(summary = "Force metrics update")
    @PostMapping("/force-update")
    public ResponseEntity<Map<String, Object>> forceMetricsUpdate() {
        try {
            realTimeMetricsService.forceUpdate();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Métricas actualizadas forzadamente");
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error forzando actualización: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene métricas de rendimiento JVM detalladas.
     * @return Métricas de rendimiento JVM
     */
    @Operation(summary = "Get detailed JVM performance metrics")
    @GetMapping("/jvm/performance")
    public ResponseEntity<Map<String, Object>> getJvmPerformanceMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Métricas de memoria detalladas
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            metrics.put("heap.used", memoryBean.getHeapMemoryUsage().getUsed());
            metrics.put("heap.max", memoryBean.getHeapMemoryUsage().getMax());
            metrics.put("heap.committed", memoryBean.getHeapMemoryUsage().getCommitted());
            metrics.put("heap.init", memoryBean.getHeapMemoryUsage().getInit());
            metrics.put("nonheap.used", memoryBean.getNonHeapMemoryUsage().getUsed());
            metrics.put("nonheap.committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
            
            // Métricas de garbage collection
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            Map<String, Object> gcMetrics = new HashMap<>();
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                Map<String, Object> gcInfo = new HashMap<>();
                gcInfo.put("collection.count", gcBean.getCollectionCount());
                gcInfo.put("collection.time", gcBean.getCollectionTime());
                gcMetrics.put(gcBean.getName(), gcInfo);
            }
            metrics.put("garbage.collectors", gcMetrics);
            
            // Métricas de carga de clases
            ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
            metrics.put("classes.loaded", classLoadingBean.getLoadedClassCount());
            metrics.put("classes.total.loaded", classLoadingBean.getTotalLoadedClassCount());
            metrics.put("classes.unloaded", classLoadingBean.getUnloadedClassCount());
            
            // Métricas de threads
            metrics.put("threads.current", ManagementFactory.getThreadMXBean().getThreadCount());
            metrics.put("threads.peak", ManagementFactory.getThreadMXBean().getPeakThreadCount());
            metrics.put("threads.daemon", ManagementFactory.getThreadMXBean().getDaemonThreadCount());
            
            metrics.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas JVM: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene métricas del sistema operativo.
     * @return Métricas del sistema operativo
     */
    @Operation(summary = "Get operating system metrics")
    @GetMapping("/os")
    public ResponseEntity<Map<String, Object>> getOperatingSystemMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            
            // Información básica del sistema
            metrics.put("os.name", System.getProperty("os.name"));
            metrics.put("os.version", System.getProperty("os.version"));
            metrics.put("os.arch", System.getProperty("os.arch"));
            metrics.put("java.version", System.getProperty("java.version"));
            metrics.put("java.vendor", System.getProperty("java.vendor"));
            metrics.put("java.home", System.getProperty("java.home"));
            
            // Métricas de CPU
            metrics.put("cpu.cores", osBean.getAvailableProcessors());
            metrics.put("cpu.load.average", osBean.getSystemLoadAverage());
            
            // Intentar obtener métricas adicionales si están disponibles
            try {
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                    
                    // Memoria del sistema
                    metrics.put("system.memory.total", sunOsBean.getTotalPhysicalMemorySize());
                    metrics.put("system.memory.free", sunOsBean.getFreePhysicalMemorySize());
                    metrics.put("system.memory.used", sunOsBean.getTotalPhysicalMemorySize() - sunOsBean.getFreePhysicalMemorySize());
                    
                    // Memoria swap
                    metrics.put("swap.total", sunOsBean.getTotalSwapSpaceSize());
                    metrics.put("swap.free", sunOsBean.getFreeSwapSpaceSize());
                    metrics.put("swap.used", sunOsBean.getTotalSwapSpaceSize() - sunOsBean.getFreeSwapSpaceSize());
                    
                    // CPU detallado
                    metrics.put("cpu.process.load", sunOsBean.getProcessCpuLoad());
                    metrics.put("cpu.system.load", sunOsBean.getSystemCpuLoad());
                    metrics.put("cpu.process.time", sunOsBean.getProcessCpuTime());
                }
            } catch (Exception e) {
                metrics.put("extended.metrics.error", "Métricas extendidas no disponibles: " + e.getMessage());
            }
            
            metrics.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas del SO: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene el estado del servicio de métricas.
     * @return Estado del servicio de métricas
     */
    @Operation(summary = "Get metrics service status")
    @GetMapping("/service/status")
    public ResponseEntity<Map<String, Object>> getMetricsServiceStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            status.put("service.healthy", realTimeMetricsService.isHealthy());
            status.put("last.update.time", realTimeMetricsService.getLastUpdateTime());
            status.put("last.update.ago", System.currentTimeMillis() - realTimeMetricsService.getLastUpdateTime());
            status.put("current.timestamp", System.currentTimeMillis());
            
            // Calcular tiempo desde la última actualización
            long timeSinceLastUpdate = System.currentTimeMillis() - realTimeMetricsService.getLastUpdateTime();
            status.put("time.since.last.update.ms", timeSinceLastUpdate);
            status.put("time.since.last.update.seconds", timeSinceLastUpdate / 1000);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo estado del servicio: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene métricas con información de tendencias.
     * @return Métricas con tendencias
     */
    @Operation(summary = "Get metrics with trends")
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getMetricsWithTrends() {
        try {
            Map<String, Object> metrics = realTimeMetricsService.getMetricsWithTrends();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas con tendencias: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
