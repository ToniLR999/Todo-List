package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.AuditLogDTO;
import com.tonilr.ToDoList.model.AuditLog;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.AuditLogRepository;
import com.tonilr.ToDoList.dto.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing audit logs with automatic cleanup and optimization.
 * Provides functionality to retrieve, create, and manage audit trail records
 * for tracking user actions and system events.
 */
@Service
@Slf4j
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private DTOMapper dtoMapper;
    
    // Configuración de retención de logs (por defecto 30 días)
    @Value("${app.audit.log.retention.days:30}")
    private int logRetentionDays;
    
    // Configuración de limpieza automática (por defecto habilitada)
    @Value("${app.audit.log.auto-cleanup:true}")
    private boolean autoCleanupEnabled;
    
    // Configuración de tamaño máximo de logs antes de limpieza forzada
    @Value("${app.audit.log.max-entries:10000}")
    private long maxLogEntries;
    
    // Configuración de frecuencia de limpieza (por defecto cada día a las 3 AM)
    @Value("${app.audit.log.cleanup.cron:0 0 3 * * *}")
    private String cleanupCron;

    /**
     * Retrieves all audit logs from the database with pagination support.
     * @return List of all audit log DTOs
     */
    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(dtoMapper::toAuditLogDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific audit log by its ID.
     * @param id The ID of the audit log to retrieve
     * @return Optional containing the audit log DTO if found
     */
    public Optional<AuditLogDTO> getLogById(Long id) {
        return auditLogRepository.findById(id)
                .map(dtoMapper::toAuditLogDTO);
    }

    /**
     * Creates a new audit log entry for tracking user actions.
     * @param user The user who performed the action
     * @param action The type of action performed
     * @param details Additional details about the action
     */
    @Transactional
    public void logAction(User user, String action, String details) {
        try {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setDetails(details);
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
            
            // Verificar si necesitamos limpieza automática
            if (autoCleanupEnabled) {
                checkAndTriggerCleanup();
            }
        } catch (Exception e) {
            log.error("Error creating audit log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Limpieza automática programada de logs antiguos.
     * Se ejecuta según la configuración de cron especificada.
     */
    @Scheduled(cron = "${app.audit.log.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void scheduledLogCleanup() {
        if (!autoCleanupEnabled) {
            log.info("Limpieza automática de logs deshabilitada");
            return;
        }
        
        try {
            performLogCleanup();
        } catch (Exception e) {
            log.error("Error durante la limpieza automática de logs: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta la limpieza de logs basándose en la configuración de retención.
     */
    @Transactional
    public void performLogCleanup() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(logRetentionDays);
            
            // Contar logs que serán eliminados
            long logsToDelete = auditLogRepository.countLogsOlderThan(cutoffDate);
            
            if (logsToDelete > 0) {
                log.info("Iniciando limpieza de {} logs más antiguos que {} días", 
                    logsToDelete, logRetentionDays);
                
                int deletedCount = auditLogRepository.deleteLogsOlderThan(cutoffDate);
                
                log.info("Limpieza completada: {} logs eliminados", deletedCount);
            } else {
                log.debug("No hay logs antiguos para limpiar");
            }
            
            // Verificar si necesitamos limpieza por tamaño
            checkSizeBasedCleanup();
            
        } catch (Exception e) {
            log.error("Error durante la limpieza de logs: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si es necesario realizar limpieza basada en el tamaño de la base de datos.
     */
    private void checkSizeBasedCleanup() {
        try {
            long totalLogs = auditLogRepository.countTotalLogs();
            
            if (totalLogs > maxLogEntries) {
                log.warn("Base de datos de logs excede el límite de {} entradas (actual: {})", 
                    maxLogEntries, totalLogs);
                
                // Calcular cuántos días de logs mantener para estar bajo el límite
                int daysToKeep = Math.max(7, logRetentionDays / 2); // Mínimo 7 días
                LocalDateTime aggressiveCutoff = LocalDateTime.now().minusDays(daysToKeep);
                
                long logsToDelete = auditLogRepository.countLogsOlderThan(aggressiveCutoff);
                if (logsToDelete > 0) {
                    int deletedCount = auditLogRepository.deleteLogsOlderThan(aggressiveCutoff);
                    log.info("Limpieza agresiva completada: {} logs eliminados para mantener límite de tamaño", 
                        deletedCount);
                }
            }
        } catch (Exception e) {
            log.error("Error durante la verificación de tamaño de logs: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verifica y activa la limpieza si es necesario.
     */
    private void checkAndTriggerCleanup() {
        try {
            long totalLogs = auditLogRepository.countTotalLogs();
            
            // Si excedemos el 80% del límite, activar limpieza
            if (totalLogs > (maxLogEntries * 0.8)) {
                log.info("Activando limpieza preventiva de logs ({} de {} entradas)", 
                    totalLogs, maxLogEntries);
                performLogCleanup();
            }
        } catch (Exception e) {
            log.error("Error durante la verificación preventiva: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene estadísticas de la base de datos de logs.
     * @return String con información de estadísticas
     */
    public String getLogStatistics() {
        try {
            long totalLogs = auditLogRepository.countTotalLogs();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(logRetentionDays);
            long oldLogs = auditLogRepository.countLogsOlderThan(cutoffDate);
            
            return String.format(
                "Total logs: %d | Logs antiguos (>%d días): %d | Retención configurada: %d días | " +
                "Límite máximo: %d | Limpieza automática: %s",
                totalLogs, logRetentionDays, oldLogs, logRetentionDays, 
                maxLogEntries, autoCleanupEnabled ? "HABILITADA" : "DESHABILITADA"
            );
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de logs: {}", e.getMessage(), e);
            return "Error obteniendo estadísticas";
        }
    }
    
    /**
     * Limpia logs de forma manual (para uso administrativo).
     * @param daysToKeep Número de días de logs a mantener
     * @return Número de logs eliminados
     */
    @Transactional
    public int manualLogCleanup(int daysToKeep) {
        try {
            if (daysToKeep < 1) {
                throw new IllegalArgumentException("Debe mantener al menos 1 día de logs");
            }
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            long logsToDelete = auditLogRepository.countLogsOlderThan(cutoffDate);
            
            if (logsToDelete > 0) {
                log.info("Limpieza manual iniciada: eliminando {} logs más antiguos que {} días", 
                    logsToDelete, daysToKeep);
                
                int deletedCount = auditLogRepository.deleteLogsOlderThan(cutoffDate);
                log.info("Limpieza manual completada: {} logs eliminados", deletedCount);
                return deletedCount;
            } else {
                log.info("No hay logs para eliminar en la limpieza manual");
                return 0;
            }
        } catch (Exception e) {
            log.error("Error durante la limpieza manual: {}", e.getMessage(), e);
            throw new RuntimeException("Error durante la limpieza manual", e);
        }
    }
}
