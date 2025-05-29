package com.tonilr.ToDoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

import lombok.Data;

// TaskDTO.java
@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private int priority;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date dueDate;
    private Long taskListId;
    private String assignedToUsername;
}