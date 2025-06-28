package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tonilr.ToDoList.dto.TaskReminderDTO;
import com.tonilr.ToDoList.service.TaskReminderService;

import java.util.List;

/**
 * REST controller for managing reminders associated with tasks.
 * Provides endpoints to create, retrieve, and delete reminders for a specific task.
 */
@RestController
@RequestMapping("/api/tasks/{taskId}/reminders")
public class TaskReminderController {

    @Autowired
    private TaskReminderService reminderService;

    /**
     * Creates a new reminder for the specified task.
     * @param taskId The ID of the task
     * @param reminderDTO The reminder details
     * @return The created reminder
     */
    @PostMapping
    public ResponseEntity<TaskReminderDTO> createReminder(
            @PathVariable Long taskId,
            @RequestBody TaskReminderDTO reminderDTO) {
        reminderDTO.setTaskId(taskId);
        return ResponseEntity.ok(reminderService.createReminder(reminderDTO));
    }

    /**
     * Retrieves all reminders for the specified task.
     * @param taskId The ID of the task
     * @return List of reminders
     */
    @GetMapping
    public ResponseEntity<List<TaskReminderDTO>> getTaskReminders(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(reminderService.getTaskReminders(taskId));
    }

    /**
     * Deletes a specific reminder for the specified task.
     * @param taskId The ID of the task
     * @param reminderId The ID of the reminder to delete
     * @return HTTP 200 if successful
     */
    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long taskId,
            @PathVariable Long reminderId) {
        reminderService.deleteReminder(reminderId);
        return ResponseEntity.ok().build();
    }
}
