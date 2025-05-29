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
import org.springframework.http.HttpStatus;
import java.time.ZoneId;
import java.util.Date;

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
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            String username = securityService.getCurrentUsername();
            TaskDTO newTask = taskService.createTask(taskDTO, username);
            return ResponseEntity.ok(newTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Obtener las tareas del usuario")
    @ApiResponse(responseCode = "200", description = "Lista de tareas encontrada")
    @GetMapping
    public ResponseEntity<?> getUserTasks(@RequestParam(required = false) Boolean completed) {
        try {
            String username = securityService.getCurrentUsername();
            List<TaskDTO> tasks = taskService.getUserTasksByStatus(username, completed != null ? completed : false);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    String.format("Error al obtener las tareas: %s - %s", 
                        e.getClass().getName(), 
                        e.getMessage())
                ));
        }
    }

    @Operation(summary = "Actualizar una tarea")
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskDTO taskDetails) {
        try {
            TaskDTO updatedTask = taskService.updateTask(taskId, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
}
