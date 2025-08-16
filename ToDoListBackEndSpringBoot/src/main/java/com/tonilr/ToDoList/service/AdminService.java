package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.SystemStatusDTO;
import com.tonilr.ToDoList.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

@Service
public class AdminService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private ScheduleService scheduleService;

    public void clearAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        }
    }

    public SystemStatusDTO getSystemInfo() {
        try {
            // Obtener información del sistema
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            String status = scheduleService.isApplicationActive() ? "UP" : "DOWN";
            String schedule = scheduleService.getCurrentSchedule();
            String version = "1.0.0";
            String environment = System.getProperty("spring.profiles.active", "default");
            
            // Calcular uptime real en segundos
            long uptimeSeconds = (System.currentTimeMillis() - runtimeBean.getStartTime()) / 1000;

            return new SystemStatusDTO(status, schedule, version, environment, uptimeSeconds);
        } catch (Exception e) {
            return new SystemStatusDTO();
        }
    }

    @CacheEvict(allEntries = true)
    public void clearUserCache() {
        // Este método limpia específicamente el caché de usuarios
    }

    @CacheEvict(allEntries = true)
    public void clearTaskCache() {
        // Este método limpia específicamente el caché de tareas
    }
}
