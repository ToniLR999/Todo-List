package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.service.TaskListService;

import io.swagger.v3.oas.annotations.Operation;

import com.tonilr.ToDoList.service.SecurityService;
import com.tonilr.ToDoList.dto.TaskListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.service.UserService;

/**
 * REST controller for managing task lists.
 * Provides endpoints for CRUD operations and searching user task lists.
 * All endpoints are secured and require authentication.
 */
@RestController
@RequestMapping("/api/lists")
public class TaskListController {

    private static final Logger log = LoggerFactory.getLogger(TaskListController.class);

    @Autowired
    private TaskListService taskListService;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    /**
     * Retrieves all task lists for the authenticated user.
     */
    @Operation(summary = "Get user's task lists")
    @GetMapping
    public ResponseEntity<List<TaskListDTO>> getUserTaskLists(Authentication authentication) {
        try {
            if (authentication == null) {
                log.error("❌ TaskListController - Authentication es null en getUserTaskLists");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            
            if (!authentication.isAuthenticated()) {
                log.error("❌ TaskListController - Usuario no autenticado en getUserTaskLists");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                log.error("❌ TaskListController - Username vacío o nulo en getUserTaskLists");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
            
            log.info(" ===== INICIO GET /api/lists =====");
            log.info("🔍 Usuario autenticado: {}", username);
            log.info("🔍 Authentication object: {}", authentication);
            log.info("🔍 Authorities: {}", authentication.getAuthorities());
            
            // Obtener el usuario completo para ver su ID
            User user = userService.findByUsername(username);
            if (user == null) {
                log.warn("⚠️ Usuario NO encontrado en BD para username: {}", username);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            if (user.getId() == null) {
                log.error("❌ Usuario encontrado pero ID es null para username: {}", username);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
            }
            
            log.info(" Usuario encontrado en BD - ID: {}, Username: {}, Email: {}", 
                user.getId(), user.getUsername(), user.getEmail());
            
            // Llamar al servicio
            log.info("🔍 Llamando a taskListService.getUserTaskLists('{}')", username);
            List<TaskListDTO> taskLists = taskListService.getUserTaskLists(username);
            
            if (taskLists == null) {
                log.warn("⚠️ taskListService.getUserTaskLists retornó null, usando lista vacía");
                taskLists = Collections.emptyList();
            }
            
            log.info("✅ Listas encontradas para usuario {} (ID: {}): {}", 
                username, user.getId(), taskLists.size());
            
            if (!taskLists.isEmpty()) {
                TaskListDTO firstList = taskLists.get(0);
                if (firstList != null) {
                    log.info("📋 Primera lista: ID={}, Name={}, Owner={}", 
                        firstList.getId(), 
                        firstList.getName(),
                        firstList.getOwnerUsername() != null ? firstList.getOwnerUsername() : "NULL");
                }
            }
            
            log.info(" ===== FIN GET /api/lists =====");
            return ResponseEntity.ok(taskLists);
            
        } catch (Exception e) {
            log.error("❌ ===== ERROR EN GET /api/lists =====");
            if (authentication != null) {
                log.error("❌ Usuario: {}", authentication.getName());
            }
            log.error("❌ Error completo:", e);
            log.error("❌ Stack trace:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }

    /**
     * Creates a new task list for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<TaskListDTO> createTaskList(@Valid @RequestBody TaskListDTO taskListDTO, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("❌ TaskListController - Usuario no autenticado en createTaskList");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            
            if (taskListDTO == null) {
                log.error("❌ TaskListController - TaskListDTO es null en createTaskList");
                return ResponseEntity.badRequest().body(null);
            }
            
            String username = securityService.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                log.error("❌ TaskListController - Username no disponible en createTaskList");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            
            // Validar datos de entrada
            if (taskListDTO.getName() == null || taskListDTO.getName().trim().isEmpty()) {
                log.warn("⚠️ TaskListController - Nombre de lista vacío en createTaskList");
                return ResponseEntity.badRequest().body(null);
            }
            
            log.info(" TaskListController - Creando lista para usuario: {}", username);
            TaskListDTO newList = taskListService.createTaskList(taskListDTO, username);
            
            if (newList == null) {
                log.warn("⚠️ TaskListController - taskListService.createTaskList retornó null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            
            log.info("✅ TaskListController - Lista creada exitosamente con ID: {}", newList.getId());
            return ResponseEntity.ok(newList);
            
        } catch (Exception e) {
            log.error("❌ TaskListController - Error en createTaskList: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates an existing task list.
     */
    @Operation(summary = "Update a task list")
    @PutMapping("/{id}")
    public ResponseEntity<TaskListDTO> updateTaskList(@PathVariable Long id, @Valid @RequestBody TaskListDTO taskListDTO, Authentication authentication) {
        try {
            TaskListDTO updatedList = taskListService.updateTaskList(id, taskListDTO);
            return ResponseEntity.ok(updatedList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Deletes a task list by its ID.
     */
    @Operation(summary = "Delete a task list")
    @DeleteMapping("/{listId}")
    public ResponseEntity<?> deleteTaskList(@PathVariable Long listId) {
        try {
            taskListService.deleteTaskList(listId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }   

    /**
     * Searches task lists by name for the authenticated user.
     */
    @Operation(summary = "Search task lists by name")
    @GetMapping("/search")
    public ResponseEntity<?> searchTaskListsByName(@RequestParam String name) {
        String username = securityService.getCurrentUsername();
        return ResponseEntity.ok(taskListService.searchUserTaskListsByName(username, name));
    }
}
