package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.model.Role;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.RoleRepository;
import com.tonilr.ToDoList.service.UserService;
import com.tonilr.ToDoList.service.SecurityService;
import com.tonilr.ToDoList.service.AuditLogService;
import com.tonilr.ToDoList.service.SystemMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryMXBean;
import org.springframework.cache.CacheManager;

/**
 * Controlador de administración para gestión de usuarios, roles y logs de auditoría.
 * Solo accesible por usuarios con rol ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private SystemMaintenanceService systemMaintenanceService;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Obtiene todos los usuarios del sistema (admin only).
     * @return Lista de usuarios
     */
    @Operation(summary = "Get all users (Admin only)")
    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        try {
            // Verificar que el usuario actual es admin
            String currentUsername = securityService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok().build(); // Temporal hasta implementar getAllUsers
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Asigna un rol a un usuario (admin only).
     * @param username Username del usuario
     * @param roleName Nombre del rol a asignar
     * @return Usuario actualizado
     */
    @Operation(summary = "Assign role to user (Admin only)")
    @PostMapping("/users/{username}/roles")
    @Transactional
    public ResponseEntity<?> assignRole(
            @PathVariable String username,
            @RequestParam String roleName) {
        try {
            // Verificar que el usuario actual es admin
            String currentUsername = securityService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validar que el rol existe
            Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElse(null);
            if (role == null) {
                return ResponseEntity.badRequest().body("Rol inválido: " + roleName);
            }

            // No permitir asignar ROLE_ADMIN a otros usuarios (solo super admin)
            if ("ROLE_ADMIN".equals(role.getName()) && !currentUser.hasRole(new Role("ROLE_ADMIN", "Administrador"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para asignar rol de administrador");
            }

            User updatedUser = userService.assignRole(username, role);
            return ResponseEntity.ok(new UserSummary(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Remueve un rol de un usuario (admin only).
     * @param username Username del usuario
     * @param roleName Nombre del rol a remover
     * @return Usuario actualizado
     */
    @Operation(summary = "Remove role from user (Admin only)")
    @DeleteMapping("/users/{username}/roles")
    @Transactional
    public ResponseEntity<?> removeRole(
            @PathVariable String username,
            @RequestParam String roleName) {
        try {
            // Verificar que el usuario actual es admin
            String currentUsername = securityService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Validar que el rol existe
            Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElse(null);
            if (role == null) {
                return ResponseEntity.badRequest().body("Rol inválido: " + roleName);
            }

            // No permitir remover ROLE_ADMIN de otros usuarios (solo super admin)
            if ("ROLE_ADMIN".equals(role.getName()) && !currentUser.hasRole(new Role("ROLE_ADMIN", "Administrador"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para remover rol de administrador");
            }

            // No permitir remover el último admin
            if ("ROLE_ADMIN".equals(role.getName()) && username.equals(currentUsername)) {
                return ResponseEntity.badRequest().body("No puedes remover tu propio rol de administrador");
            }

            User updatedUser = userService.removeRole(username, role);
            return ResponseEntity.ok(new UserSummary(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Promueve un usuario a administrador (super admin only).
     * @param username Username del usuario a promover
     * @return Usuario actualizado
     */
    @Operation(summary = "Promote user to admin (Super Admin only)")
    @PostMapping("/users/{username}/promote-admin")
    @Transactional
    public ResponseEntity<?> promoteToAdmin(@PathVariable String username) {
        try {
            // Verificar que el usuario actual es admin
            String currentUsername = securityService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            User updatedUser = userService.promoteToAdmin(username);
            return ResponseEntity.ok(new UserSummary(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Degrada un administrador (super admin only).
     * @param username Username del administrador a degradar
     * @return Usuario actualizado
     */
    @Operation(summary = "Demote admin user (Super Admin only)")
    @PostMapping("/users/{username}/demote-admin")
    @Transactional
    public ResponseEntity<?> demoteFromAdmin(@PathVariable String username) {
        try {
            // Verificar que el usuario actual es admin
            String currentUsername = securityService.getCurrentUsername();
            User currentUser = userService.findByUsername(currentUsername);
            
            if (!currentUser.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // No permitir degradarse a sí mismo
            if (username.equals(currentUsername)) {
                return ResponseEntity.badRequest().body("No puedes degradarte a ti mismo");
            }

            User updatedUser = userService.demoteFromAdmin(username);
            return ResponseEntity.ok(new UserSummary(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas de la base de datos de logs de auditoría.
     * @return Estadísticas de logs
     */
    @Operation(summary = "Get audit log statistics (Admin only)")
    @GetMapping("/logs/statistics")
    public ResponseEntity<String> getLogStatistics() {
        try {
            String stats = auditLogService.getLogStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error obteniendo estadísticas: " + e.getMessage());
        }
    }

    /**
     * Ejecuta limpieza manual de logs de auditoría.
     * @param daysToKeep Número de días de logs a mantener
     * @return Resultado de la limpieza
     */
    @Operation(summary = "Manual audit log cleanup (Admin only)")
    @PostMapping("/logs/cleanup")
    public ResponseEntity<String> manualLogCleanup(@RequestParam(defaultValue = "30") int daysToKeep) {
        try {
            if (daysToKeep < 1 || daysToKeep > 365) {
                return ResponseEntity.badRequest().body("Días a mantener debe estar entre 1 y 365");
            }
            
            int deletedCount = auditLogService.manualLogCleanup(daysToKeep);
            return ResponseEntity.ok(String.format("Limpieza completada: %d logs eliminados. Manteniendo %d días.", 
                deletedCount, daysToKeep));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante la limpieza: " + e.getMessage());
        }
    }

    /**
     * Ejecuta limpieza automática de logs de auditoría.
     * @return Resultado de la limpieza automática
     */
    @Operation(summary = "Trigger automatic audit log cleanup (Admin only)")
    @PostMapping("/logs/cleanup/auto")
    public ResponseEntity<String> triggerAutoLogCleanup() {
        try {
            auditLogService.performLogCleanup();
            return ResponseEntity.ok("Limpieza automática ejecutada correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante la limpieza automática: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas del sistema y mantenimiento.
     * @return Estadísticas del sistema
     */
    @Operation(summary = "Get system maintenance statistics (Admin only)")
    @GetMapping("/maintenance/statistics")
    public ResponseEntity<String> getSystemMaintenanceStatistics() {
        try {
            String stats = systemMaintenanceService.getSystemStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error obteniendo estadísticas del sistema: " + e.getMessage());
        }
    }

    /**
     * Ejecuta mantenimiento manual del sistema.
     * @return Resultado del mantenimiento
     */
    @Operation(summary = "Execute manual system maintenance (Admin only)")
    @PostMapping("/maintenance/execute")
    public ResponseEntity<String> executeSystemMaintenance() {
        try {
            String result = systemMaintenanceService.executeManualMaintenance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error durante el mantenimiento del sistema: " + e.getMessage());
        }
    }

    /**
     * Obtiene información del sistema.
     * @return Información del sistema
     */
    @Operation(summary = "Get system information (Admin only)")
    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        try {
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("status", "UP");
            systemInfo.put("version", "1.0.0");
            systemInfo.put("environment", System.getProperty("spring.profiles.active", "default"));
            systemInfo.put("uptimeSeconds", (System.currentTimeMillis() - runtimeBean.getStartTime()) / 1000);
            systemInfo.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Limpia todos los caches de la aplicación.
     * @return Resultado de la limpieza
     */
    @Operation(summary = "Clear all caches (Admin only)")
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearAllCaches() {
        try {
            if (cacheManager != null) {
                cacheManager.getCacheNames()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
                return ResponseEntity.ok("All caches cleared successfully");
            }
            return ResponseEntity.ok("No cache manager available");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error clearing caches: " + e.getMessage());
        }
    }

    /**
     * DTO para mostrar información resumida de usuarios.
     */
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String timezone;
        private Set<String> roles;

        public UserSummary(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.timezone = user.getTimezone();
            this.roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getTimezone() { return timezone; }
        public Set<String> getRoles() { return roles; }
    }
}
