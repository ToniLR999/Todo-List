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

/**
 * REST controller for managing task lists.
 * Provides endpoints for CRUD operations and searching user task lists.
 * All endpoints are secured and require authentication.
 */
@RestController
@RequestMapping("/api/lists")
public class TaskListController {

    @Autowired
    private TaskListService taskListService;
    
    @Autowired
    private SecurityService securityService;

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
    public ResponseEntity<?> getUserTaskLists() {
        try {
            String username = securityService.getCurrentUsername();
            List<TaskListDTO> lists = taskListService.getUserTaskLists(username);
            return ResponseEntity.ok(lists);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
