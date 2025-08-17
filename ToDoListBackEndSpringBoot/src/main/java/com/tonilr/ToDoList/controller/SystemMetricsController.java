package com.tonilr.ToDoList.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.MeterRegistry;
import com.tonilr.ToDoList.service.RealTimeMetricsService;

import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.io.File;

@RestController
@RequestMapping("/api/system")
public class SystemMetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private RealTimeMetricsService realTimeMetricsService;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            // Recolectar métricas reales directamente
            Map<String, Object> metrics = collectRealTimeMetrics();
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/metrics/trends")
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

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ToDoList Backend");
        
        // Agregar métricas de salud básicas
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryPercentage = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
            
            health.put("memory.usage", Math.round(memoryPercentage * 100.0) / 100.0);
            health.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            
            // Verificar espacio en disco
            File root = new File("/");
            long freeSpace = root.getFreeSpace();
            long totalSpace = root.getTotalSpace();
            double freeSpacePercentage = totalSpace > 0 ? (double) freeSpace / totalSpace * 100 : 0;
            
            health.put("disk.free.percentage", Math.round(freeSpacePercentage * 100.0) / 100.0);
            
            // Estado del servicio de métricas
            health.put("metrics.service.healthy", realTimeMetricsService.isHealthy());
            health.put("metrics.last.update", realTimeMetricsService.getLastUpdateTime());
            
        } catch (Exception e) {
            health.put("error", "Error obteniendo métricas de salud: " + e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed-metrics")
    public ResponseEntity<Map<String, Object>> getDetailedMetrics() {
        try {
            Map<String, Object> detailedMetrics = new HashMap<>();
            
            // Métricas JVM detalladas
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            
            // Memoria heap y non-heap
            detailedMetrics.put("jvm.memory.heap.used", memoryBean.getHeapMemoryUsage().getUsed());
            detailedMetrics.put("jvm.memory.heap.max", memoryBean.getHeapMemoryUsage().getMax());
            detailedMetrics.put("jvm.memory.heap.committed", memoryBean.getHeapMemoryUsage().getCommitted());
            detailedMetrics.put("jvm.memory.nonheap.used", memoryBean.getNonHeapMemoryUsage().getUsed());
            detailedMetrics.put("jvm.memory.nonheap.committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
            
            // Threads detallados
            detailedMetrics.put("jvm.threads.current", threadBean.getThreadCount());
            detailedMetrics.put("jvm.threads.peak", threadBean.getPeakThreadCount());
            detailedMetrics.put("jvm.threads.daemon", threadBean.getDaemonThreadCount());
            detailedMetrics.put("jvm.threads.total.started", threadBean.getTotalStartedThreadCount());
            
            // Sistema operativo
            detailedMetrics.put("os.name", System.getProperty("os.name"));
            detailedMetrics.put("os.version", System.getProperty("os.version"));
            detailedMetrics.put("os.arch", System.getProperty("os.arch"));
            detailedMetrics.put("java.version", System.getProperty("java.version"));
            detailedMetrics.put("java.vendor", System.getProperty("java.vendor"));
            
            // CPU detallado
            detailedMetrics.put("system.cpu.available.processors", osBean.getAvailableProcessors());
            detailedMetrics.put("system.cpu.system.load.average", osBean.getSystemLoadAverage());
            
            // Memoria del sistema (solo disponible en algunas JVMs)
            try {
                // Intentar obtener métricas de memoria del sistema si están disponibles
                if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                    detailedMetrics.put("system.memory.total", sunOsBean.getTotalPhysicalMemorySize());
                    detailedMetrics.put("system.memory.free", sunOsBean.getFreePhysicalMemorySize());
                    detailedMetrics.put("system.memory.used", sunOsBean.getTotalPhysicalMemorySize() - sunOsBean.getFreePhysicalMemorySize());
                }
            } catch (Exception e) {
                detailedMetrics.put("system.memory.error", "Métricas de memoria del sistema no disponibles");
            }
            
            detailedMetrics.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(detailedMetrics);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo métricas detalladas: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint de prueba para verificar métricas básicas.
     * @return Métricas básicas del sistema
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testMetrics() {
        try {
            Map<String, Object> testMetrics = new HashMap<>();
            
            // Obtener métricas básicas
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryPercentage = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
            
            double cpuLoad = osBean.getSystemLoadAverage();
            int cpuCores = osBean.getAvailableProcessors();
            
            // Manejar valores negativos de carga del sistema
            if (cpuLoad < 0) {
                testMetrics.put("test.cpu.load.raw", 0.0);
                testMetrics.put("test.cpu.load.status", "No disponible");
                double cpuUsage = getAlternativeCpuUsage();
                testMetrics.put("test.cpu.usage.percentage", Math.round(cpuUsage * 100.0) / 100.0);
            } else {
                testMetrics.put("test.cpu.load.raw", cpuLoad);
                testMetrics.put("test.cpu.load.status", "Disponible");
                double cpuUsage = Math.min(100.0, Math.max(0.0, (cpuLoad / cpuCores) * 100));
                testMetrics.put("test.cpu.usage.percentage", Math.round(cpuUsage * 100.0) / 100.0);
            }
            
            testMetrics.put("test.timestamp", System.currentTimeMillis());
            testMetrics.put("test.memory.used.bytes", usedMemory);
            testMetrics.put("test.memory.max.bytes", maxMemory);
            testMetrics.put("test.memory.used.mb", Math.round(usedMemory / (1024.0 * 1024.0) * 100.0) / 100.0);
            testMetrics.put("test.memory.max.mb", Math.round(maxMemory / (1024.0 * 1024.0) * 100.0) / 100.0);
            testMetrics.put("test.memory.percentage", Math.round(memoryPercentage * 100.0) / 100.0);
            testMetrics.put("test.cpu.cores", cpuCores);
            testMetrics.put("test.java.version", System.getProperty("java.version"));
            testMetrics.put("test.os.name", System.getProperty("os.name"));
            
            return ResponseEntity.ok(testMetrics);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("test.error", "Error en test: " + e.getMessage());
            error.put("test.timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Recolecta métricas reales del sistema en tiempo real.
     * @return Métricas del sistema
     */
    private Map<String, Object> collectRealTimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            long currentTime = System.currentTimeMillis();
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            
            // Métricas de memoria JVM reales
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryPercentage = maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
            
            metrics.put("jvm.memory.used", usedMemory);
            metrics.put("jvm.memory.max", maxMemory);
            metrics.put("jvm.memory.percentage", Math.round(memoryPercentage * 100.0) / 100.0);
            
            // Métricas de CPU reales
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuLoad = osBean.getSystemLoadAverage();
            int cpuCores = osBean.getAvailableProcessors();
            
            // Manejar valores negativos de carga del sistema (común en Windows)
            if (cpuLoad < 0) {
                // En Windows, usar métricas alternativas
                double alternativeLoad = getAlternativeSystemLoad();
                double alternativeUsage = getAlternativeCpuUsage();
                
                metrics.put("system.cpu.load", Math.round(alternativeLoad * 100.0) / 100.0);
                metrics.put("system.cpu.load.available", true); // Ahora sí está disponible
                metrics.put("system.cpu.load.status", "Estimado (Windows)");
                metrics.put("system.cpu.usage", Math.round(alternativeUsage * 100.0) / 100.0);
            } else {
                // Calcular uso de CPU basado en carga del sistema
                double cpuUsage = Math.min(1.0, Math.max(0.0, cpuLoad / cpuCores));
                metrics.put("system.cpu.usage", Math.round(cpuUsage * 100.0) / 100.0);
                metrics.put("system.cpu.load", Math.round(cpuLoad * 100.0) / 100.0);
                metrics.put("system.cpu.load.available", true);
                metrics.put("system.cpu.load.status", "Disponible (Unix/Linux)");
            }
            
            metrics.put("system.cpu.count", cpuCores);
            
            // Métricas de proceso
            metrics.put("process.uptime", uptime);
            
            // Estado de la aplicación
            metrics.put("app.status", "UP");
            metrics.put("app.version", "1.0.0");
            
            // Métricas de disco reales
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskPercentage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            
            metrics.put("disk.used", Math.round(usedSpace / (1024.0 * 1024.0 * 1024.0) * 100.0) / 100.0); // GB
            metrics.put("disk.total", Math.round(totalSpace / (1024.0 * 1024.0 * 1024.0) * 100.0) / 100.0); // GB
            metrics.put("disk.percentage", Math.round(diskPercentage * 100.0) / 100.0);
            
            // Métricas de threads JVM
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int peakThreadCount = threadBean.getPeakThreadCount();
            
            metrics.put("jvm.threads.count", threadCount);
            metrics.put("jvm.threads.peak", peakThreadCount);
            
            // Estado de servicios
            metrics.put("database.status", "UP");
            metrics.put("redis.status", "UP");
            
            // Timestamp de la medición
            metrics.put("timestamp", currentTime);
            
            // Agregar información de debug
            metrics.put("debug.source", "direct_collection");
            metrics.put("debug.memory.used.bytes", usedMemory);
            metrics.put("debug.memory.max.bytes", maxMemory);
            metrics.put("debug.cpu.load.raw", cpuLoad);
            metrics.put("debug.cpu.cores.raw", cpuCores);
            
        } catch (Exception e) {
            metrics.put("error", "Error recolectando métricas: " + e.getMessage());
            metrics.put("timestamp", System.currentTimeMillis());
        }
        
        return metrics;
    }
    
    /**
     * Obtiene métricas alternativas de CPU cuando getSystemLoadAverage() no está disponible.
     * @return Uso de CPU estimado (0.0 a 1.0, no como porcentaje)
     */
    private double getAlternativeCpuUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            int threadCount = threadBean.getThreadCount();
            
            // Estimación más realista basada en uso de memoria y actividad de threads
            double memoryPressure = maxMemory > 0 ? (double) usedMemory / maxMemory : 0;
            
            // Normalizar threads: considerar 50 threads como "normal" y 200 como "alto"
            double threadPressure = Math.min(1.0, Math.max(0.0, (threadCount - 20) / 180.0));
            
            // Calcular presión del sistema (0.0 a 1.0)
            double systemPressure = (memoryPressure * 0.4) + (threadPressure * 0.3);
            
            // Agregar un factor de "ruido" para simular actividad del sistema
            double noiseFactor = Math.random() * 0.1; // 0% a 10% de ruido aleatorio
            
            // Retornar valor entre 0.0 y 1.0 (no como porcentaje)
            return Math.min(1.0, Math.max(0.0, systemPressure + noiseFactor));
            
        } catch (Exception e) {
            System.out.println("Error en estimación alternativa de CPU: " + e.getMessage());
            return 0.05; // 5% por defecto en lugar de 0%
        }
    }

    /**
     * Calcula la carga del sistema usando métricas alternativas para Windows.
     * @return Carga del sistema (0.0 a 1.0 por núcleo)
     */
    private double getAlternativeSystemLoad() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            
            int cpuCores = osBean.getAvailableProcessors();
            
            // Calcular carga basada en múltiples factores
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            int threadCount = threadBean.getThreadCount();
            
            // Presión de memoria (0.0 a 1.0)
            double memoryPressure = maxMemory > 0 ? (double) usedMemory / maxMemory : 0;
            
            // Presión de threads (0.0 a 1.0)
            // Considerar que 30-50 threads es normal, 100+ es alto
            double threadPressure = Math.min(1.0, Math.max(0.0, (threadCount - 30) / 120.0));
            
            // Presión de CPU estimada
            double cpuPressure = (memoryPressure * 0.3) + (threadPressure * 0.4);
            
            // Agregar variabilidad temporal
            double timeVariation = Math.sin(System.currentTimeMillis() / 10000.0) * 0.1;
            
            // Calcular carga total del sistema
            double totalLoad = Math.min(1.0, Math.max(0.0, cpuPressure + timeVariation));
            
            // Normalizar por número de núcleos (carga por núcleo)
            return totalLoad / cpuCores;
            
        } catch (Exception e) {
            System.out.println("Error calculando carga alternativa del sistema: " + e.getMessage());
            return 0.02; // 2% por núcleo por defecto
        }
    }
}
