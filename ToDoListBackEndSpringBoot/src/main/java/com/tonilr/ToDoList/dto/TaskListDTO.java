package com.tonilr.ToDoList.dto;

import java.util.List;

public class TaskListDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;        // AÑADIR ownerId
    private String ownerUsername; // Mantener para compatibilidad
    private List<TaskDTO> tasks;

    // Constructor por defecto
    public TaskListDTO() {}

    // Constructor con parámetros
    public TaskListDTO(Long id, String name, String description, Long ownerId, String ownerUsername, List<TaskDTO> tasks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.tasks = tasks;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getOwnerId() { return ownerId; }           // AÑADIR getter
    public String getOwnerUsername() { return ownerUsername; }
    public List<TaskDTO> getTasks() { return tasks; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }           // AÑADIR setter
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public void setTasks(List<TaskDTO> tasks) { this.tasks = tasks; }

    // toString para debugging
    @Override
    public String toString() {
        return "TaskListDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", ownerId=" + ownerId +                    // AÑADIR al toString
                ", ownerUsername='" + ownerUsername + '\'' +
                ", tasks=" + (tasks != null ? tasks.size() : 0) + " items" +
                '}';
    }
}