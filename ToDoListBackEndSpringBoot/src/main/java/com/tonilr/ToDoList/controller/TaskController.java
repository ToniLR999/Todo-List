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

@Tag(name = "Tasks", description = "API de gestión de tareas")
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SecurityService securityService;

    @Operation(summary = "Crear una nueva tarea")
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

    @Operation(summary = "Obtener las tareas del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de tareas encontrada")
    @GetMapping
    public ResponseEntity<?> getTasks(
        @RequestParam(required = false) Boolean showCompleted,
        @RequestParam(required = false) Long listId) {
        
        String username = securityService.getCurrentUsername();
        List<TaskDTO> tasks;
        
        if (listId != null) {
            List<CacheableTaskDTO> cachedTasks = taskService.getTasksByList(listId, username);
            tasks = cachedTasks.stream()
                .map(CacheableTaskDTO::toTaskDTO)
                .collect(Collectors.toList());
        } else {
            boolean showCompletedValue = showCompleted != null ? showCompleted : false;
            List<CacheableTaskDTO> cachedTasks = taskService.getUserTasksByStatus(username, showCompletedValue);
            tasks = cachedTasks.stream()
                .map(CacheableTaskDTO::toTaskDTO)
                .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Actualizar una tarea")
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

    @Operation(summary = "Eliminar una tarea")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getTasksByPriority(@PathVariable int priority) {
        String username = securityService.getCurrentUsername();
        List<CacheableTaskDTO> cachedTasks = taskService.getUserTasksByPriority(username, priority);
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/duedate")
    public ResponseEntity<?> getTasksByDueDate(@RequestParam String dueDate) {
        String username = securityService.getCurrentUsername();
        LocalDateTime dateTime = LocalDateTime.parse(dueDate);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return ResponseEntity.ok(taskService.getUserTasksByDueDate(username, date));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTasksByTitle(@RequestParam String title) {
        String username = securityService.getCurrentUsername();
        List<CacheableTaskDTO> cachedTasks = taskService.searchUserTasksByTitle(username, title);
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Obtener detalles de una tarea")
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

    @GetMapping("/filter")
    public ResponseEntity<List<TaskDTO>> getFilteredTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String completed,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) Long listId,
            Authentication authentication) {
        
        String username = authentication.getName();
        Boolean completedBool = completed != null ? Boolean.parseBoolean(completed) : null;
        
        List<CacheableTaskDTO> cachedTasks = taskService.getFilteredTasks(search, completedBool, priority, dateFilter, username, listId);
        
        // Convertir de vuelta a TaskDTO para el frontend
        List<TaskDTO> tasks = cachedTasks.stream()
            .map(CacheableTaskDTO::toTaskDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(tasks);
    }
}
