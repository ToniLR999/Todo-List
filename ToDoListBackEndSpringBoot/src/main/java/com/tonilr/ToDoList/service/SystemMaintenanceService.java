package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de mantenimiento del sistema que ejecuta tareas de limpieza
 * y optimización automáticamente para mantener el rendimiento.
 */
@Service
@Slf4j
public class SystemMaintenanceService {

    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    // Configuración de mantenimiento
    @Value("${app.maintenance.enabled:true}")
    private boolean maintenanceEnabled;
    
    @Value("${app.maintenance.cache-cleanup.enabled:true}")
    private boolean cacheCleanupEnabled;
    
    @Value("${app.maintenance.memory-threshold:80}")
    private int memoryThreshold;

    /**
     * Mantenimiento diario del sistema (ejecuta a las 2 AM).
     * Incluye limpieza de logs y optimización de caché.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyMaintenance() {
        if (!maintenanceEnabled) {
            log.info("Mantenimiento diario deshabilitado");
            return;
        }
        
        try {
            log.info("Iniciando mantenimiento diario del sistema");
            
            // Limpieza de logs de auditoría
            auditLogService.performLogCleanup();
            
            // Limpieza de caché si está habilitada
            if (cacheCleanupEnabled) {
                cleanupCache();
            }
            
            // Verificación de memoria
            checkMemoryUsage();
            
            log.info("Mantenimiento diario completado exitosamente");
            
        } catch (Exception e) {
            log.error("Error durante el mantenimiento diario: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpieza de caché para liberar memoria.
     */
    private void cleanupCache() {
        try {
            if (cacheManager != null) {
                log.info("Iniciando limpieza de caché");
                
                cacheManager.getCacheNames().forEach(cacheName -> {
                    try {
                        cacheManager.getCache(cacheName).clear();
                        log.debug("Caché '{}' limpiado", cacheName);
                    } catch (Exception e) {
                        log.warn("Error limpiando caché '{}': {}", cacheName, e.getMessage());
                    }
                });
                
                log.info("Limpieza de caché completada");
            } else {
                log.debug("CacheManager no disponible, omitiendo limpieza de caché");
            }
        } catch (Exception e) {
            log.error("Error durante la limpieza de caché: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica el uso de memoria y registra estadísticas.
     */
    private void checkMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long uptime = runtimeBean.getUptime();
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            log.info("Estadísticas de memoria - Uso: {} MB / {} MB ({:.1f}%) | Uptime: {} días",
                usedMemory / (1024 * 1024),
                maxMemory / (1024 * 1024),
                memoryUsagePercent,
                TimeUnit.MILLISECONDS.toDays(uptime)
            );
            
            // Alerta si el uso de memoria es alto
            if (memoryUsagePercent > memoryThreshold) {
                log.warn("⚠️ Uso de memoria alto: {:.1f}% (umbral: {}%)", 
                    memoryUsagePercent, memoryThreshold);
                
                // Forzar limpieza de caché si es necesario
                if (cacheCleanupEnabled && cacheManager != null) {
                    log.info("Forzando limpieza de caché por alto uso de memoria");
                    cleanupCache();
                }
            }
            
        } catch (Exception e) {
            log.error("Error verificando uso de memoria: {}", e.getMessage(), e);
        }
    }

    /**
     * Mantenimiento semanal del sistema (ejecuta los domingos a las 3 AM).
     * Incluye tareas más intensivas de limpieza.
     */
    @Scheduled(cron = "0 0 3 * * 0")
    public void weeklyMaintenance() {
        if (!maintenanceEnabled) {
            log.info("Mantenimiento semanal deshabilitado");
            return;
        }
        
        try {
            log.info("Iniciando mantenimiento semanal del sistema");
            
            // Limpieza más agresiva de logs (mantener solo 7 días)
            auditLogService.manualLogCleanup(7);
            
            // Limpieza completa de caché
            if (cacheCleanupEnabled) {
                cleanupCache();
            }
            
            // Verificación de memoria
            checkMemoryUsage();
            
            log.info("Mantenimiento semanal completado exitosamente");
            
        } catch (Exception e) {
            log.error("Error durante el mantenimiento semanal: {}", e.getMessage(), e);
        }
    }

    /**
     * Ejecuta mantenimiento manual del sistema.
     * @return Mensaje de resultado
     */
    public String executeManualMaintenance() {
        try {
            log.info("Ejecutando mantenimiento manual del sistema");
            
            // Limpieza de logs
            auditLogService.performLogCleanup();
            
            // Limpieza de caché
            if (cacheCleanupEnabled) {
                cleanupCache();
            }
            
            // Verificación de memoria
            checkMemoryUsage();
            
            log.info("Mantenimiento manual completado exitosamente");
            return "Mantenimiento manual completado exitosamente";
            
        } catch (Exception e) {
            log.error("Error durante el mantenimiento manual: {}", e.getMessage(), e);
            return "Error durante el mantenimiento: " + e.getMessage();
        }
    }

    /**
     * Obtiene estadísticas del sistema.
     * @return Estadísticas del sistema
     */
    public String getSystemStatistics() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long uptime = runtimeBean.getUptime();
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            return String.format(
                "Sistema - Memoria: %.1f%% | Uptime: %d días | Mantenimiento: %s | " +
                "Limpieza caché: %s | Umbral memoria: %d%%",
                memoryUsagePercent,
                TimeUnit.MILLISECONDS.toDays(uptime),
                maintenanceEnabled ? "HABILITADO" : "DESHABILITADO",
                cacheCleanupEnabled ? "HABILITADA" : "DESHABILITADA",
                memoryThreshold
            );
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas del sistema: {}", e.getMessage(), e);
            return "Error obteniendo estadísticas del sistema";
        }
    }
}
