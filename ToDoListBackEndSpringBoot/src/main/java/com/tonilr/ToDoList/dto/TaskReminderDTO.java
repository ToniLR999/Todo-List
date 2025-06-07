package com.tonilr.ToDoList.dto;

import java.time.LocalDateTime;

import com.tonilr.ToDoList.model.ReminderType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TaskReminderDTO {
    private Long id;
    private Long taskId;
    private LocalDateTime reminderTime;
    private ReminderType reminderType;
    private boolean isSent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public LocalDateTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalDateTime reminderTime) { this.reminderTime = reminderTime; }

    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }

    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }
}
