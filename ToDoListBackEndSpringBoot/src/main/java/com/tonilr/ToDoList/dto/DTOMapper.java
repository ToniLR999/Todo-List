package com.tonilr.ToDoList.dto;

import com.tonilr.ToDoList.model.*;

import java.util.stream.Collectors;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * Data Transfer Object (DTO) mapper component.
 * Provides methods to convert between entity objects and DTOs,
 * ensuring proper data transformation and preventing direct entity exposure
 * in API responses while maintaining data integrity.
 */
@Component
public class DTOMapper {
    
    /**
     * Converts User entity to UserDTO for API responses.
     * @param user User entity to convert
     * @return UserDTO or null if user is null
     */
    public UserDTO toUserDTO(User user) {
        if (user == null) return null;
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    /**
     * Converts UserRegistrationDTO to User entity for registration.
     * @param dto Registration DTO containing user data
     * @return User entity
     */
    public User toUser(UserRegistrationDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return user;
    }

    /**
     * Converts User entity to UserProfileDTO for profile operations.
     * @param user User entity to convert
     * @return UserProfileDTO with profile information
     */
    public UserProfileDTO toUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setTimezone(user.getTimezone());
        return dto;
    }

    /**
     * Converts Task entity to TaskDTO for API responses.
     * Handles date conversion and nested object mapping.
     * @param task Task entity to convert
     * @return TaskDTO with all task information
     */
    public TaskDTO toTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCompleted(task.isCompleted());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setDueDate(task.getDueDate() != null ? 
            Date.from(task.getDueDate().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);
        if (task.getTaskList() != null) {
            dto.setTaskListId(task.getTaskList().getId());
            dto.setTaskListName(task.getTaskList().getName());
        }
        dto.setAssignedTo(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null);
        return dto;
    }

    /**
     * Converts TaskDTO to Task entity for persistence.
     * Handles date conversion from Date to LocalDateTime.
     * @param dto TaskDTO to convert
     * @return Task entity
     */
    public Task toTask(TaskDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCompleted(dto.isCompleted());
        task.setPriority(dto.getPriority());
        task.setDueDate(dto.getDueDate() != null ? 
            dto.getDueDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        return task;
    }

    /**
     * Converts TaskList entity to TaskListDTO for API responses.
     * Includes nested task conversion and owner information.
     * @param taskList TaskList entity to convert
     * @return TaskListDTO with all list information
     */
    public TaskListDTO toTaskListDTO(TaskList taskList) {
        TaskListDTO dto = new TaskListDTO();
        dto.setId(taskList.getId());
        dto.setName(taskList.getName());
        dto.setDescription(taskList.getDescription());
        dto.setOwnerUsername(taskList.getOwner() != null ? taskList.getOwner().getUsername() : null);
        dto.setTasks(taskList.getTasks().stream()
                .map(this::toTaskDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    /**
     * Converts TaskListDTO to TaskList entity for persistence.
     * @param dto TaskListDTO to convert
     * @return TaskList entity
     */
    public TaskList toTaskList(TaskListDTO dto) {
        TaskList taskList = new TaskList();
        taskList.setName(dto.getName());
        taskList.setDescription(dto.getDescription());
        return taskList;
    }

    /**
     * Converts AuditLog entity to AuditLogDTO for API responses.
     * @param auditLog AuditLog entity to convert
     * @return AuditLogDTO or null if auditLog is null
     */
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
