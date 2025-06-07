package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tonilr.ToDoList.dto.TaskReminderDTO;
import com.tonilr.ToDoList.service.TaskReminderService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/reminders")
public class TaskReminderController {
    @Autowired
    private TaskReminderService reminderService;

    @PostMapping
    public ResponseEntity<TaskReminderDTO> createReminder(
            @PathVariable Long taskId,
            @RequestBody TaskReminderDTO reminderDTO) {
        reminderDTO.setTaskId(taskId);
        return ResponseEntity.ok(reminderService.createReminder(reminderDTO));
    }

    @GetMapping
    public ResponseEntity<List<TaskReminderDTO>> getTaskReminders(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(reminderService.getTaskReminders(taskId));
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long taskId,
            @PathVariable Long reminderId) {
        reminderService.deleteReminder(reminderId);
        return ResponseEntity.ok().build();
    }
}
