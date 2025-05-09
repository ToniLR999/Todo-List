package com.tonilr.ToDoList.dto;

import com.tonilr.ToDoList.model.*;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class DTOMapper {
    
    public UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public User toUser(UserRegistrationDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    public TaskDTO toTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCompleted(task.isCompleted());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setDueDate(task.getDueDate());
        dto.setTaskListId(task.getTaskList() != null ? task.getTaskList().getId() : null);
        dto.setAssignedToUsername(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null);
        return dto;
    }

    public Task toTask(TaskDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate());
        return task;
    }

    public TaskListDTO toTaskListDTO(TaskList taskList) {
        TaskListDTO dto = new TaskListDTO();
        dto.setId(taskList.getId());
        dto.setName(taskList.getName());
        dto.setOwnerUsername(taskList.getOwner() != null ? taskList.getOwner().getUsername() : null);
        dto.setTasks(taskList.getTasks().stream()
                .map(this::toTaskDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public TaskList toTaskList(TaskListDTO dto) {
        TaskList taskList = new TaskList();
        taskList.setName(dto.getName());
        return taskList;
    }

    public AuditLogDTO toAuditLogDTO(AuditLog auditLog) {
        if (auditLog == null) return null;
        
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId());
        dto.setUsername(auditLog.getUser().getUsername());
        dto.setAction(auditLog.getAction());
        dto.setDetails(auditLog.getDetails());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}
