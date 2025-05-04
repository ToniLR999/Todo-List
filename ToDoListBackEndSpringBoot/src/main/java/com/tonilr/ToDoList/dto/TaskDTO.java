package com.tonilr.ToDoList.dto;

import java.time.LocalDateTime;

import lombok.Data;

// TaskDTO.java
@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private int priority;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private Long taskListId;
    private String assignedToUsername;
}