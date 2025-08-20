package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tonilr.ToDoList.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import com.tonilr.ToDoList.dto.CacheableTaskDTO;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import java.util.Collections;

/**
 * REST controller for managing user tasks.
 * Provides endpoints for CRUD operations, filtering, and searching tasks.
 * All endpoints are secured and require authentication.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SecurityService securityService;



    /**
     * Creates a new task for the authenticated user.
     */
    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("❌ TaskController - Usuario no autenticado en createTask");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            
            if (taskDTO == null) {
                log.error("❌ TaskController - TaskDTO es null en createTask");
                return ResponseEntity.badRequest().body(null);
            }
            
            String username = securityService.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                log.error("❌ TaskController - Username no disponible en createTask");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            
            log.info("🔍 TaskController - Creando tarea para usuario: {}", username);
            TaskDTO newTask = taskService.createTask(taskDTO, username);
            
            if (newTask == null) {
                log.warn("⚠️ TaskController - taskService.createTask retornó null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            
            log.info("✅ TaskController - Tarea creada exitosamente con ID: {}", newTask.getId());
            return ResponseEntity.ok(newTask);
            
        } catch (Exception e) {
            log.error("❌ TaskController - Error en createTask: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Retrieves all tasks for the authenticated user, optionally filtered by completion status or list.
     */
    @Operation(summary = "Get user tasks")
    @ApiResponse(responseCode = "200", description = "Task list found")
    @GetMapping
    public ResponseEntity<?> getTasks(
        @RequestParam(required = false) Boolean showCompleted,
        @RequestParam(required = false) Long listId) {
        
        try {
            String username = securityService.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                log.error("❌ TaskController - Username no disponible en getTasks");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
            }
            
            List<TaskDTO> tasks;
            
            if (listId != null) {
                // Validar listId
                if (listId <= 0) {
                    log.warn("⚠️ TaskController - listId inválido: {}", listId);
                    return ResponseEntity.badRequest().body(Collections.emptyList());
                }
                
                // Get tasks by list
                List<CacheableTaskDTO> cachedTasks = taskService.getTasksByList(listId, username);
                if (cachedTasks == null) {
                    cachedTasks = Collections.emptyList();
                }
                
                tasks = cachedTasks.stream()
                    .filter(task -> task != null)
                    .map(task -> {
                        try {
                            return task.toTaskDTO();
                        } catch (Exception e) {
                            log.error("❌ TaskController - Error convirtiendo tarea a DTO: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(task -> task != null)
                    .collect(Collectors.toList());
            } else {
                // Get tasks by completion status
                boolean showCompletedValue = showCompleted != null ? showCompleted : false;
                List<CacheableTaskDTO> cachedTasks = taskService.getUserTasksByStatus(username, showCompletedValue);
                
                if (cachedTasks == null) {
                    cachedTasks = Collections.emptyList();
                }
                
                tasks = cachedTasks.stream()
                    .filter(task -> task != null)
                    .map(task -> {
                        try {
                            return task.toTaskDTO();
                        } catch (Exception e) {
                            log.error("❌ TaskController - Error convirtiendo tarea a DTO: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(task -> task != null)
                    .collect(Collectors.toList());
            }
            
            log.info("✅ TaskController - getTasks completado exitosamente. Tareas obtenidas: {}", tasks.size());
            return ResponseEntity.ok(tasks);
            
        } catch (Exception e) {
            log.error("❌ TaskController - Error en getTasks: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Updates an existing task.
     */
    @Operation(summary = "Update a task")
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDTO taskDetails,
            Authentication authentication) {
        try {
            TaskDTO updatedTask = taskService.updateTask(taskId, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Deletes a task by its ID.
     */
    @Operation(summary = "Delete a task")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves tasks by priority for the authenticated user.
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getTasksByPriority(@PathVariable int priority) {
        String username = securityService.getCurrentUsername();
        List<CacheableTaskDTO> cachedTasks = taskService.getUserTasksByPriority(username, priority);
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves tasks by due date for the authenticated user.
     */
    @GetMapping("/duedate")
    public ResponseEntity<?> getTasksByDueDate(@RequestParam String dueDate) {
        String username = securityService.getCurrentUsername();
        LocalDateTime dateTime = LocalDateTime.parse(dueDate);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(taskService.getUserTasksByDueDate(username, date));
    }

    /**
     * Searches tasks by title for the authenticated user.
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTasksByTitle(@RequestParam String title) {
        String username = securityService.getCurrentUsername();
        List<CacheableTaskDTO> cachedTasks = taskService.searchUserTasksByTitle(username, title);
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Retrieves the details of a specific task.
     */
    @Operation(summary = "Get task details")
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskDetails(@PathVariable Long taskId) {
        try {
            String username = securityService.getCurrentUsername();
            TaskDTO task = taskService.getTaskDetails(taskId, username);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves tasks with advanced filtering options.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TaskDTO>> getFilteredTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String completed,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) Long taskListId,
            Authentication authentication) {
        
        log.info("🔍 TaskController - /filter llamado");
        
        try {
            if (authentication == null) {
                log.error("❌ TaskController - Usuario NO autenticado en /filter");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            
            if (!authentication.isAuthenticated()) {
                log.error("❌ TaskController - Usuario NO autenticado correctamente en /filter");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
            
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                log.error("❌ TaskController - Username vacío o nulo en /filter");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }
            
            log.info("✅ TaskController - Usuario autenticado: {} en /filter", username);
            
            // Validar parámetros de entrada
            Boolean completedBool = null;
            if (completed != null && !completed.trim().isEmpty()) {
                try {
                    completedBool = Boolean.parseBoolean(completed);
                } catch (Exception e) {
                    log.warn("⚠️ TaskController - Parámetro 'completed' inválido: {}, usando null", completed);
                }
            }
            
            // Validar priority
            if (priority != null && !priority.equals("all")) {
                try {
                    int priorityInt = Integer.parseInt(priority);
                    if (priorityInt < 1 || priorityInt > 3) {
                        log.warn("⚠️ TaskController - Prioridad fuera de rango: {}, usando 'all'", priority);
                        priority = "all";
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ TaskController - Prioridad inválida: {}, usando 'all'", priority);
                    priority = "all";
                }
            }
            
            log.info("🔍 TaskController - Llamando a taskService.getFilteredTasks con parámetros: search={}, completed={}, priority={}, dateFilter={}, taskListId={}", 
                search, completedBool, priority, dateFilter, taskListId);
            
            List<CacheableTaskDTO> cachedTasks = taskService.getFilteredTasks(search, completedBool, priority, dateFilter, username, taskListId);
            
            if (cachedTasks == null) {
                log.warn("⚠️ TaskController - taskService.getFilteredTasks retornó null, usando lista vacía");
                cachedTasks = Collections.emptyList();
            }
            
            log.info("✅ TaskController - Tareas obtenidas del servicio: {}", cachedTasks.size());
            
            // Convertir de vuelta a TaskDTO para el frontend
            List<TaskDTO> tasks = cachedTasks.stream()
                .filter(task -> task != null) // Filtrar tareas nulas
                .map(task -> {
                    try {
                        return task.toTaskDTO();
                    } catch (Exception e) {
                        log.error("❌ TaskController - Error convirtiendo tarea a DTO: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(task -> task != null) // Filtrar DTOs nulos
                .collect(Collectors.toList());
            
            log.info("✅ TaskController - Tareas convertidas a DTO: {}", tasks.size());
            return ResponseEntity.ok(tasks);
            
        } catch (Exception e) {
            log.error("❌ TaskController - Error inesperado en /filter: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.emptyList());
        }
    }


}
        