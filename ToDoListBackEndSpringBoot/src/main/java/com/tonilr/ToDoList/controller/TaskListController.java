package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.service.TaskListService;

import io.swagger.v3.oas.annotations.Operation;

import com.tonilr.ToDoList.service.SecurityService;
import com.tonilr.ToDoList.dto.TaskListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskListController {
    @Autowired
    private TaskListService taskListService;
    
    @Autowired
    private SecurityService securityService;

    @Operation(summary = "Crear una nueva lista de tareas")
    @PostMapping
    public ResponseEntity<?> createTaskList(@RequestBody TaskListDTO taskListDTO) {
        try {
            String username = securityService.getCurrentUsername();
            TaskListDTO newList = taskListService.createTaskList(taskListDTO, username);
            return ResponseEntity.ok(newList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Obtener las listas de tareas del usuario")
    @GetMapping
    public ResponseEntity<?> getUserTaskLists() {
        try {
            String username = securityService.getCurrentUsername();
            List<TaskListDTO> lists = taskListService.getUserTaskLists(username);
            return ResponseEntity.ok(lists);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar una lista de tareas")
    @PutMapping("/{listId}")
    public ResponseEntity<?> updateTaskList(
            @PathVariable Long listId,
            @RequestBody TaskListDTO taskListDetails) {
        try {
            TaskListDTO updatedList = taskListService.updateTaskList(listId, taskListDetails);
            return ResponseEntity.ok(updatedList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar una lista de tareas")
    @DeleteMapping("/{listId}")
    public ResponseEntity<?> deleteTaskList(@PathVariable Long listId) {
        try {
            taskListService.deleteTaskList(listId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
