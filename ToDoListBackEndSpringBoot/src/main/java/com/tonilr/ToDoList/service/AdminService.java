package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    public void clearAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        }
    }

    public Map<String, Object> getSystemInfo() {
        try {
            // Obtener información del sistema
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            String status = "UP"; // Siempre UP si el backend está funcionando
            String version = "1.0.0";
            String environment = System.getProperty("spring.profiles.active", "default");
            
            // Calcular uptime real en segundos
            long uptimeSeconds = (System.currentTimeMillis() - runtimeBean.getStartTime()) / 1000;

            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("status", status);
            systemInfo.put("version", version);
            systemInfo.put("environment", environment);
            systemInfo.put("uptimeSeconds", uptimeSeconds);
            systemInfo.put("timestamp", System.currentTimeMillis());

            return systemInfo;
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "ERROR");
            errorInfo.put("error", e.getMessage());
            return errorInfo;
        }
    }
}
