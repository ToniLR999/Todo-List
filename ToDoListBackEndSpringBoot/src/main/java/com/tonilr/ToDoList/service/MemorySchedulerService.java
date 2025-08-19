package com.tonilr.ToDoList.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemorySchedulerService {

    private final MemoryMonitorService memoryMonitorService;

    /**
     * Monitorea la memoria cada 5 minutos
     */
    @Scheduled(fixedRate = 300000) // 5 minutos
    public void monitorMemoryPeriodically() {
        try {
            memoryMonitorService.logMemoryStatus();
            
            // Si la memoria está crítica, forzar GC
            if (memoryMonitorService.isMemoryCritical()) {
                log.warn("🚨 MEMORIA CRÍTICA DETECTADA - Forzando Garbage Collector");
                memoryMonitorService.forceGarbageCollection();
            }
        } catch (Exception e) {
            log.error("❌ Error en monitoreo automático de memoria", e);
        }
    }

    /**
     * Limpieza de memoria cada hora
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora
    public void cleanupMemory() {
        try {
            log.info("🧹 Ejecutando limpieza programada de memoria...");
            memoryMonitorService.forceGarbageCollection();
            
            // Log del estado después de la limpieza
            memoryMonitorService.logMemoryStatus();
        } catch (Exception e) {
            log.error("❌ Error en limpieza programada de memoria", e);
        }
    }
}
