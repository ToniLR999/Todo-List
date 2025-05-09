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

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private DTOMapper dtoMapper;

    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAll().stream()
                .map(dtoMapper::toAuditLogDTO)
                .collect(Collectors.toList());
    }

    public Optional<AuditLogDTO> getLogById(Long id) {
        return auditLogRepository.findById(id)
                .map(dtoMapper::toAuditLogDTO);
    }

    public void logAction(User user, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
