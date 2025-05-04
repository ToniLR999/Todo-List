package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.service.TaskService;
import com.tonilr.ToDoList.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SecurityService securityService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            String username = securityService.getCurrentUsername();
            Task newTask = taskService.createTask(task, username);
            return ResponseEntity.ok(newTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserTasks(
            @RequestParam(required = false) Boolean completed) {
        try {
            String username = securityService.getCurrentUsername();
            List<Task> tasks;
            if (completed != null) {
                tasks = taskService.getUserTasksByStatus(username, completed);
            } else {
                tasks = taskService.getUserTasks(username);
            }
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody Task taskDetails) {
        try {
            Task updatedTask = taskService.updateTask(taskId, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
