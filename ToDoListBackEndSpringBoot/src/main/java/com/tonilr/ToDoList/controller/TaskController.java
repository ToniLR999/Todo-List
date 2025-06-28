package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.exception.ErrorResponse;
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
import com.tonilr.ToDoList.service.CacheService;

/**
 * REST controller for managing user tasks.
 * Provides endpoints for CRUD operations, filtering, and searching tasks.
 * All endpoints are secured and require authentication.
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    private CacheService cacheService;

    /**
     * Creates a new task for the authenticated user.
     */
    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO taskDTO, Authentication authentication) {
        try {
            String username = securityService.getCurrentUsername();
            TaskDTO newTask = taskService.createTask(taskDTO, username);
            return ResponseEntity.ok(newTask);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
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
        
        String username = securityService.getCurrentUsername();
        List<TaskDTO> tasks;
        
        if (listId != null) {
            // Get tasks by list
            List<CacheableTaskDTO> cachedTasks = taskService.getTasksByList(listId, username);
            tasks = cachedTasks.stream()
                .map(CacheableTaskDTO::toTaskDTO)
                .collect(Collectors.toList());
        } else {
            // Get tasks by completion status
            boolean showCompletedValue = showCompleted != null ? showCompleted : false;
            List<CacheableTaskDTO> cachedTasks = taskService.getUserTasksByStatus(username, showCompletedValue);
            tasks = cachedTasks.stream()
                .map(CacheableTaskDTO::toTaskDTO)
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(tasks);
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
        
        String username = authentication.getName();
        System.out.println("🔄 CONTROLLER: Recibiendo petición de filtrado");
        System.out.println("🔄 CONTROLLER: Usuario: " + username);
        System.out.println("🔄 CONTROLLER: TaskListId: " + taskListId);
        System.out.println("🔄 CONTROLLER: Completed: " + completed);
        System.out.println("🔄 CONTROLLER: Search: " + search);
        System.out.println("🔄 CONTROLLER: Priority: " + priority);
        System.out.println("🔄 CONTROLLER: DateFilter: " + dateFilter);
        
        Boolean completedBool = completed != null ? Boolean.parseBoolean(completed) : null;
        
        List<CacheableTaskDTO> cachedTasks = taskService.getFilteredTasks(search, completedBool, priority, dateFilter, username, taskListId);
        
        // Convertir de vuelta a TaskDTO para el frontend
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        
        System.out.println("🔄 CONTROLLER: Tareas devueltas: " + tasks.size());
        
        return ResponseEntity.ok(tasks);
    }

    /**
     * Endpoint to clear all task-related caches (for debugging or admin use).
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        try {
            // Limpiar caché de Redis
            cacheService.evictAllCache();
            return ResponseEntity.ok("Caché limpiado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al limpiar caché: " + e.getMessage());
        }
    }
}
        