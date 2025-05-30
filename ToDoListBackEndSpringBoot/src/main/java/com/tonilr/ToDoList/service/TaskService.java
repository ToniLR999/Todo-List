package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.DTOMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.exception.BadRequestException;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.exception.UnauthorizedException;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, String username) {
        System.out.println("Creando nueva tarea - Invalidando caché");
        User user = userService.findByUsername(username);
        Task task = dtoMapper.toTask(taskDTO);
        task.setAssignedTo(user);
        task.setCreatedAt(new Date());
        Task savedTask = taskRepository.save(task);

        // Notificación si la tarea es de alta prioridad
        if (task.getPriority() == 1) {
            emailService.sendSimpleEmail(
                user.getEmail(),
                "Nueva tarea importante creada",
                "Has creado una tarea de alta prioridad: " + task.getTitle()
            );
        }

        auditLogService.logAction(user, "CREAR_TAREA", "Tarea creada: " + task.getTitle());
        return dtoMapper.toTaskDTO(savedTask);
    }

    @Cacheable(value = "tasks", key = "#username")
    public List<TaskDTO> getUserTasks(String username) {
        System.out.println("Obteniendo tareas de la base de datos para: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedTo(user)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "tasks", key = "#username + '_' + #completed")
    public List<TaskDTO> getUserTasksByStatus(String username, boolean completed) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedToAndCompleted(user, completed)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasksByPriority(String username, int priority) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedToAndPriority(user, priority)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasksByDueDate(String username, Date dueDate) {
        User user = userService.findByUsername(username);
        LocalDateTime localDateTime = dueDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        return taskRepository.findByAssignedToAndDueDateBefore(user, localDateTime)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    public List<TaskDTO> searchUserTasksByTitle(String username, String title) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedToAndTitleContainingIgnoreCase(user, title)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskDTO updateTask(Long taskId, TaskDTO taskDetails) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));
        
        // Verificar si el usuario tiene permiso
        if (!securityService.isOwner(task.getAssignedTo().getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta tarea");
        }
    
        // Validaciones de negocio
        if (taskDetails.getTitle() == null || taskDetails.getTitle().trim().isEmpty()) {
            throw new BadRequestException("El título de la tarea no puede estar vacío");
        }
    
        // Actualizar la tarea
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        
        Task updatedTask = taskRepository.save(task);
        auditLogService.logAction(task.getAssignedTo(), "ACTUALIZAR_TAREA", "Tarea actualizada: " + updatedTask.getTitle());
        return dtoMapper.toTaskDTO(updatedTask);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));
        if (!securityService.isOwner(task.getAssignedTo().getId())) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta tarea");
        }
        auditLogService.logAction(task.getAssignedTo(), "ELIMINAR_TAREA", "Tarea eliminada: " + task.getTitle());
        taskRepository.delete(task);
    }

    public TaskDTO getTaskDetails(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));
        
        if (!securityService.isOwner(task.getAssignedTo().getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta tarea");
        }
        
        return dtoMapper.toTaskDTO(task);
    }

    public List<TaskDTO> getFilteredTasks(String username, String search, String status, String priority, String dateFilter) {
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedTo(user);

        // Aplicar filtros
        if (search != null && !search.isEmpty()) {
            tasks = tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                              (task.getDescription() != null && 
                               task.getDescription().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
        }

        if (status != null && !status.equals("all")) {
            boolean completed = status.equals("completed");
            tasks = tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
        }

        if (priority != null && !priority.equals("all")) {
            int priorityValue = Integer.parseInt(priority);
            tasks = tasks.stream()
                .filter(task -> task.getPriority() == priorityValue)
                .collect(Collectors.toList());
        }

        if (dateFilter != null && !dateFilter.equals("all")) {
            LocalDateTime now = LocalDateTime.now();
            tasks = tasks.stream()
                .filter(task -> {
                    if (task.getDueDate() == null) return false;
                    LocalDateTime taskDate = task.getDueDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    switch (dateFilter) {
                        case "today":
                            return taskDate.toLocalDate().equals(now.toLocalDate());
                        case "week":
                            return taskDate.isAfter(now) && 
                                   taskDate.isBefore(now.plusWeeks(1));
                        case "month":
                            return taskDate.isAfter(now) && 
                                   taskDate.isBefore(now.plusMonths(1));
                        case "overdue":
                            return taskDate.isBefore(now);
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
        }

        return tasks.stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }
}
