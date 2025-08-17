package com.tonilr.ToDoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Data;

/**
 * Data Transfer Object for task operations.
 * Represents task data for API requests and responses, including
 * proper date formatting and task list associations.
 * Uses Lombok @Data annotation for automatic getter/setter generation.
 */
@Data
public class TaskDTO {

    private Long id;

    private String title;

    private String description;

    private boolean completed;

    private int priority;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date dueDate;

    private Long taskListId;

    private String taskListName;

    private String assignedTo;

    private Long assignedToId;
    
    private Long userId;
    
}