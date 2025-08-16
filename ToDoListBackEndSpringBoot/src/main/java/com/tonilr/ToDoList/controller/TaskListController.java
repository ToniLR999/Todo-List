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
     * Creates a new task list for the authenticated user.
     */
    @Operation(summary = "Create a new task list")
    @PostMapping
    public ResponseEntity<TaskListDTO> createTaskList(@Valid @RequestBody TaskListDTO taskListDTO, Authentication authentication) {
        try {
            String username = securityService.getCurrentUsername();
            TaskListDTO newList = taskListService.createTaskList(taskListDTO, username);
            return ResponseEntity.ok(newList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Retrieves all task lists for the authenticated user.
     */
    @Operation(summary = "Get user's task lists")
    @GetMapping
    public ResponseEntity<List<TaskListDTO>> getUserTaskLists(Authentication authentication) {
        try {
            String username = authentication.getName();
            log.info(" ===== INICIO GET /api/lists =====");
            log.info("🔍 Usuario autenticado: {}", username);
            log.info("🔍 Authentication object: {}", authentication);
            log.info("🔍 Authorities: {}", authentication.getAuthorities());
            
            // Obtener el usuario completo para ver su ID
            User user = userService.findByUsername(username);
            if (user != null) {
                log.info(" Usuario encontrado en BD - ID: {}, Username: {}, Email: {}", 
                    user.getId(), user.getUsername(), user.getEmail());
            } else {
                log.warn("⚠️ Usuario NO encontrado en BD para username: {}", username);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            // Llamar al servicio
            log.info("🔍 Llamando a taskListService.getUserTaskLists('{}')", username);
            List<TaskListDTO> taskLists = taskListService.getUserTaskLists(username);
            
            log.info("✅ Listas encontradas para usuario {} (ID: {}): {}", 
                username, user.getId(), taskLists.size());
            
            if (!taskLists.isEmpty()) {
                log.info("📋 Primera lista: ID={}, Name={}, Owner={}", 
                    taskLists.get(0).getId(), 
                    taskLists.get(0).getName(),
                    taskLists.get(0).getOwnerUsername() != null ? taskLists.get(0).getOwnerUsername() : "NULL");
            }
            
            log.info(" ===== FIN GET /api/lists =====");
            return ResponseEntity.ok(taskLists);
            
        } catch (Exception e) {
            log.error("❌ ===== ERROR EN GET /api/lists =====");
            log.error("❌ Usuario: {}", authentication.getName());
            log.error("❌ Error completo:", e);
            log.error("❌ Stack trace:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
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
