package com.tonilr.ToDoList.dto;

import java.util.List;

import lombok.Data;


// TaskListDTO.java
@Data
public class TaskListDTO {
    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private List<TaskDTO> tasks;
}