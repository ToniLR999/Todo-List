package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.SystemStatusDTO;
import com.tonilr.ToDoList.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            adminService.clearAllCaches();
            return ResponseEntity.ok(Map.of("message", "Caché limpiado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error limpiando caché: " + e.getMessage()));
        }
    }

    @GetMapping("/system-info")
    public ResponseEntity<SystemStatusDTO> getSystemInfo() {
        try {
            SystemStatusDTO systemInfo = adminService.getSystemInfo();
            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new SystemStatusDTO());
        }
    }
}
