package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.DTOMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.exception.BadRequestException;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.exception.UnauthorizedException;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskListRepository;
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

    @Autowired
    private TaskListRepository taskListRepository;

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, String username) {
        System.out.println("Creando nueva tarea - Invalidando caché");
        System.out.println("TaskListId recibido: " + taskDTO.getTaskListId()); // Log del taskListId recibido
        
        User user = userService.findByUsername(username);
        Task task = dtoMapper.toTask(taskDTO);
        task.setAssignedTo(user);
        task.setUser(user);
        task.setCreatedAt(new Date());
        
        // Si hay un taskListId, asignar la tarea a esa lista
        if (taskDTO.getTaskListId() != null) {
            System.out.println("Asignando tarea a la lista con ID: " + taskDTO.getTaskListId()); // Log cuando se asigna a una lista
            TaskList taskList = taskListRepository.findById(taskDTO.getTaskListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
            
            // Verificar que el usuario es el propietario de la lista
            if (!taskList.getOwner().equals(user)) {
                throw new UnauthorizedException("No tienes permiso para añadir tareas a esta lista");
            }
            
            task.setTaskList(taskList);
            System.out.println("Tarea asignada correctamente a la lista: " + taskList.getName()); // Log de confirmación
        } else {
            System.out.println("No se proporcionó taskListId - La tarea no se asignará a ninguna lista"); // Log cuando no hay lista
        }
        
        // Convertir la fecha a la zona horaria del usuario
        if (taskDTO.getDueDate() != null) {
            String timezone = user.getTimezone();
            if (timezone == null || timezone.isEmpty()) {
                timezone = "Europe/Madrid";
            }
            ZoneId userZone = ZoneId.of(timezone);
            LocalDateTime dueDate = task.getDueDate();
            task.setDueDate(dueDate);
        }
        
        // Guardar la tarea en la base de datos
        Task savedTask = taskRepository.save(task);
        System.out.println("Tarea guardada con ID: " + savedTask.getId() + 
                          (savedTask.getTaskList() != null ? 
                          " en la lista: " + savedTask.getTaskList().getName() : 
                          " sin lista asignada")); // Log final con el resultado

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

    public List<TaskDTO> getUserTasks(String username) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedTo(user)
            .stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName()); // <-- Añade esto
                }
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getUserTasksByStatus(String username, boolean showCompleted) {
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndCompleted(user, showCompleted);
    
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName()); // <-- Añade esto también aquí
                }
                return dto;
            })
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

    public List<TaskDTO> getFilteredTasks(String search, Boolean completed, String priority, String dateFilter, String username, Long taskListId) {
        System.out.println("Filtros recibidos - completed: " + completed + ", taskListId: " + taskListId); // Debug
        
        User user = userService.findByUsername(username);
        List<Task> tasks;
        
        if (taskListId != null) {
            // Si hay un taskListId, obtener las tareas de esa lista
            TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
            
            if (!taskList.getOwner().equals(user)) {
                throw new UnauthorizedException("No tienes acceso a esta lista");
            }
            
            tasks = taskRepository.findByTaskList(taskList);
            
            // Aplicar filtro de completed si está presente
            if (completed != null) {
                tasks = tasks.stream()
                    .filter(task -> task.isCompleted() == completed)
                    .collect(Collectors.toList());
            }
        } else {
            // Si no hay taskListId, usar el filtrado normal
            if (completed == null) {
                tasks = taskRepository.findByAssignedTo(user);
            } else {
                tasks = taskRepository.findByAssignedToAndCompleted(user, completed);
            }
        }
        
        System.out.println("Tareas encontradas: " + tasks.size()); // Debug
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName());
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByList(Long listId, String username) {
        User user = userService.findByUsername(username);
        TaskList taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        
        if (!taskList.getOwner().equals(user)) {
            throw new UnauthorizedException("No tienes acceso a esta lista");
        }
        
        // Usar el repositorio de tareas para obtener las tareas
        List<Task> tasks = taskRepository.findByTaskList(taskList);
        
        // Convertir cada tarea a DTO individualmente
        return tasks.stream()
            .map(task -> dtoMapper.toTaskDTO(task))
            .collect(Collectors.toList());
    }
}
