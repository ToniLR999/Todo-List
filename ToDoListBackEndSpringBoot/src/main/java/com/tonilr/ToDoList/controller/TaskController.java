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
            tasks = taskService.getTasksByList(listId, username);
        } else {
            boolean showCompletedValue = showCompleted != null ? showCompleted : false;
            tasks = taskService.getUserTasksByStatus(username, showCompletedValue);
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
        return ResponseEntity.ok(taskService.getUserTasksByPriority(username, priority));
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
        return ResponseEntity.ok(taskService.searchUserTasksByTitle(username, title));
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
        @RequestParam(required = false) Long taskListId,
        Authentication authentication
    ) {
        System.out.println("Filtros recibidos en el backend:"); // Debug
        System.out.println("completed: " + completed); // Debug
        System.out.println("priority: " + priority); // Debug
        System.out.println("dateFilter: " + dateFilter); // Debug
        System.out.println("listId: " + taskListId); // Debug

        List<TaskDTO> tasks = taskService.getFilteredTasks(
            search, 
            completed != null ? Boolean.parseBoolean(completed) : null,
            priority,
            dateFilter,
            authentication.getName(),
            taskListId
        );

        System.out.println("Número de tareas encontradas: " + tasks.size()); // Debug
        return ResponseEntity.ok(tasks);
    }
}
