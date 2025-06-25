package com.tonilr.ToDoList.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class CacheableTaskDTO {
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
    private String assignedToUsername;
    
    // Constructor vacío
    public CacheableTaskDTO() {}
    
    // Constructor desde TaskDTO
    public CacheableTaskDTO(TaskDTO taskDTO) {
        this.id = taskDTO.getId();
        this.title = taskDTO.getTitle();
        this.description = taskDTO.getDescription();
        this.completed = taskDTO.isCompleted();
        this.priority = taskDTO.getPriority();
        this.createdAt = taskDTO.getCreatedAt();
        this.dueDate = taskDTO.getDueDate() != null ? 
            Date.from(taskDTO.getDueDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
        this.taskListId = taskDTO.getTaskListId();
        this.taskListName = taskDTO.getTaskListName();
        this.assignedToUsername = taskDTO.getAssignedTo();
    }
    
    // Método para convertir de vuelta a TaskDTO
    public TaskDTO toTaskDTO() {
        TaskDTO dto = new TaskDTO();
        dto.setId(this.id);
        dto.setTitle(this.title);
        dto.setDescription(this.description);
        dto.setCompleted(this.completed);
        dto.setPriority(this.priority);
        dto.setCreatedAt(this.createdAt);
        dto.setDueDate(this.dueDate != null ? 
            Date.from(this.dueDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
        dto.setTaskListId(this.taskListId);
        dto.setTaskListName(this.taskListName);
        dto.setAssignedTo(this.assignedToUsername);
        return dto;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    
    public Long getTaskListId() { return taskListId; }
    public void setTaskListId(Long taskListId) { this.taskListId = taskListId; }
    
    public String getTaskListName() { return taskListName; }
    public void setTaskListName(String taskListName) { this.taskListName = taskListName; }
    
    public String getAssignedToUsername() { return assignedToUsername; }
    public void setAssignedToUsername(String assignedToUsername) { this.assignedToUsername = assignedToUsername; }
}