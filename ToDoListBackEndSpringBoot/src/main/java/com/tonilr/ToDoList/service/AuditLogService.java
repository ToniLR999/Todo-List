package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.AuditLogDTO;
import com.tonilr.ToDoList.model.AuditLog;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.AuditLogRepository;
import com.tonilr.ToDoList.dto.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Service class for managing audit logs.
 * Provides functionality to retrieve, create, and manage audit trail records
 * for tracking user actions and system events.
 */
@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private DTOMapper dtoMapper;

    /**
     * Retrieves all audit logs from the database.
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
    public void logAction(User user, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
