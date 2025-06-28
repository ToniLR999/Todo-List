package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.AuditLogDTO;
import com.tonilr.ToDoList.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for accessing audit logs.
 * Provides endpoints to retrieve all logs or a specific log by ID.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Retrieves all audit logs.
     * @return List of audit logs
     */
    @GetMapping
    public ResponseEntity<List<AuditLogDTO>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    /**
     * Retrieves a specific audit log by its ID.
     * @param id The ID of the audit log
     * @return The audit log, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getLogById(@PathVariable Long id) {
        return auditLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
