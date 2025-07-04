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
import com.tonilr.ToDoList.dto.CacheableTaskDTO;

/**
 * Service class for managing task operations.
 * Provides comprehensive functionality for creating, retrieving, updating, deleting,
 * and filtering tasks with proper authorization, caching, and audit logging.
 */
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

    /**
     * Creates a new task for the specified user with list assignment and timezone handling.
     * @param taskDTO Task data to create
     * @param username Username of the task owner
     * @return Created task DTO
     */
    @CacheEvict(value = {"tasks", "taskCounts", "userStats"}, allEntries = true)
    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, String username) {
        // System.out.println("Creando nueva tarea - Invalidando caché");
        // System.out.println("TaskListId recibido: " + taskDTO.getTaskListId()); // Log del taskListId recibido
        
        User user = userService.findByUsername(username);
        Task task = dtoMapper.toTask(taskDTO);
        task.setAssignedTo(user);
        task.setUser(user);
        task.setCreatedAt(new Date());
        
        // Si hay un taskListId, asignar la tarea a esa lista
        if (taskDTO.getTaskListId() != null) {
            // System.out.println("Asignando tarea a la lista con ID: " + taskDTO.getTaskListId()); // Log cuando se asigna a una lista
            TaskList taskList = taskListRepository.findById(taskDTO.getTaskListId())
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
            
            // Verificar que el usuario es el propietario de la lista
            if (!taskList.getOwner().equals(user)) {
                throw new UnauthorizedException("No tienes permiso para añadir tareas a esta lista");
            }
            
            task.setTaskList(taskList);
            // System.out.println("Tarea asignada correctamente a la lista: " + taskList.getName()); // Log de confirmación
        } else {
            // System.out.println("No se proporcionó taskListId - La tarea no se asignará a ninguna lista"); // Log cuando no hay lista
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
        // System.out.println("Tarea guardada con ID: " + savedTask.getId() + 
        //                   (savedTask.getTaskList() != null ? 
        //                   " en la lista: " + savedTask.getTaskList().getName() : 
        //                   " sin lista asignada")); // Log final con el resultado

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

    /**
     * Retrieves all tasks for a user with caching.
     * @param username Username to get tasks for
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_all'")
    public List<CacheableTaskDTO> getUserTasks(String username) {
        // System.out.println("OBTENIENDO TODAS LAS TAREAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedTo(user);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName());
                }
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves tasks by completion status for a user with caching.
     * @param username Username to get tasks for
     * @param showCompleted Whether to show completed or pending tasks
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_status_' + #showCompleted")
    public List<CacheableTaskDTO> getUserTasksByStatus(String username, boolean showCompleted) {
        // System.out.println("OBTENIENDO TAREAS POR ESTADO DESDE BASE DE DATOS para usuario: " + username + ", completed: " + showCompleted);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndCompleted(user, showCompleted);
    
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName());
                }
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks by priority level for a user with caching.
     * @param username Username to get tasks for
     * @param priority Priority level (1-3)
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_priority_' + #priority")
    public List<CacheableTaskDTO> getUserTasksByPriority(String username, int priority) {
        // System.out.println("OBTENIENDO TAREAS POR PRIORIDAD DESDE BASE DE DATOS para usuario: " + username + ", prioridad: " + priority);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndPriority(user, priority);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks by due date for a user with caching.
     * @param username Username to get tasks for
     * @param dueDate Due date to filter by
     * @return List of task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_duedate_' + #dueDate")
    public List<TaskDTO> getUserTasksByDueDate(String username, Date dueDate) {
        // System.out.println("OBTENIENDO TAREAS POR FECHA DESDE BASE DE DATOS para usuario: " + username + ", fecha: " + dueDate);
        User user = userService.findByUsername(username);
        LocalDateTime localDateTime = dueDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        return taskRepository.findByAssignedToAndDueDateBefore(user, localDateTime)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    /**
     * Searches tasks by title for a user with caching.
     * @param username Username to search tasks for
     * @param title Title to search for (case-insensitive)
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_search_' + #title")
    public List<CacheableTaskDTO> searchUserTasksByTitle(String username, String title) {
        // System.out.println("BUSCANDO TAREAS POR TÍTULO DESDE BASE DE DATOS para usuario: " + username + ", título: " + title);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndTitleContainingIgnoreCase(user, title);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing task with authorization checks and validation.
     * @param taskId ID of the task to update
     * @param taskDetails Updated task data
     * @return Updated task DTO
     */
    @CacheEvict(value = {"tasks", "taskCounts", "userStats"}, allEntries = true)
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
        task.setDueDate(taskDetails.getDueDate() != null ? 
            taskDetails.getDueDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        
        Task updatedTask = taskRepository.save(task);
        auditLogService.logAction(task.getAssignedTo(), "ACTUALIZAR_TAREA", "Tarea actualizada: " + updatedTask.getTitle());
        return dtoMapper.toTaskDTO(updatedTask);
    }

    /**
     * Deletes a task with authorization checks.
     * @param taskId ID of the task to delete
     */
    @CacheEvict(value = {"tasks", "taskCounts", "userStats"}, allEntries = true) 
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

    /**
     * Retrieves detailed information for a specific task with authorization checks.
     * @param taskId ID of the task to get details for
     * @param username Username for authorization check
     * @return Task DTO
     */
    @Cacheable(value = "tasks", key = "'task_' + #taskId")
    public TaskDTO getTaskDetails(Long taskId, String username) {
        // System.out.println("OBTENIENDO DETALLES DE TAREA DESDE BASE DE DATOS para ID: " + taskId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));
        
        if (!securityService.isOwner(task.getAssignedTo().getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta tarea");
        }
        
        return dtoMapper.toTaskDTO(task);
    }

    /**
     * Retrieves filtered tasks based on multiple criteria with caching.
     * @param search Search term for task title
     * @param completed Completion status filter
     * @param priority Priority level filter
     * @param dateFilter Date filter criteria
     * @param username Username to filter tasks for
     * @param taskListId Task list ID filter
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_filtered_' + #search + '_' + #completed + '_' + #priority + '_' + #dateFilter + '_' + #taskListId")
    public List<CacheableTaskDTO> getFilteredTasks(String search, Boolean completed, String priority, String dateFilter, String username, Long taskListId) {
        // System.out.println("🔄 BACKEND: OBTENIENDO TAREAS FILTRADAS");
        // System.out.println("🔄 BACKEND: Usuario: " + username);
        // System.out.println("🔄 BACKEND: Lista ID: " + taskListId);
        // System.out.println("🔄 BACKEND: Search: " + search);
        // System.out.println("🔄 BACKEND: Completed: " + completed);
        // System.out.println("🔄 BACKEND: Priority: " + priority);
        // System.out.println("🔄 BACKEND: DateFilter: " + dateFilter);
        
        User user = userService.findByUsername(username);
        // System.out.println("🔄 BACKEND: Usuario encontrado: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        List<Task> tasks;
        if (taskListId != null) {
            // System.out.println("🔄 BACKEND: Filtrando por lista ID: " + taskListId);
            tasks = taskRepository.findByAssignedToAndTaskListId(user, taskListId);
            // System.out.println("🔄 BACKEND: Tareas encontradas para lista " + taskListId + ": " + tasks.size());
        } else {
            // System.out.println("🔄 BACKEND: Obteniendo todas las tareas del usuario");
            tasks = taskRepository.findByAssignedTo(user);
            // System.out.println("🔄 BACKEND: Total tareas del usuario: " + tasks.size());
        }
        
        // Log de cada tarea encontrada
        for (Task task : tasks) {
            // System.out.println("🔄 BACKEND: Tarea - ID: " + task.getId() + 
            //                   ", Título: " + task.getTitle() + 
            //                   ", Lista: " + (task.getTaskList() != null ? task.getTaskList().getId() : "null") +
            //                   ", Usuario: " + task.getAssignedTo().getUsername());
        }
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName());
                }
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks for a specific task list with caching.
     * @param listId ID of the task list
     * @param username Username for authorization check
     * @return List of cacheable task DTOs
     */
    @Cacheable(value = "tasks", key = "'user_' + #username + '_list_' + #listId")
    public List<CacheableTaskDTO> getTasksByList(Long listId, String username) {
        // System.out.println("OBTENIENDO TAREAS POR LISTA DESDE BASE DE DATOS para lista: " + listId + ", usuario: " + username);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndTaskListId(user, listId);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                if (task.getTaskList() != null) {
                    dto.setTaskListId(task.getTaskList().getId());
                    dto.setTaskListName(task.getTaskList().getName());
                }
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks with optional list filtering.
     * @param listId Optional task list ID to filter by
     * @return List of task entities
     */
    @Cacheable(value = "tasks", key = "#listId != null ? 'list_' + #listId : 'all'")
    public List<Task> getTasks(Long listId) {
        // System.out.println("OBTENIENDO TAREAS DESDE BASE DE DATOS para listId: " + listId);
        if (listId != null) {
            return taskRepository.findByTaskListId(listId);
        } else {
            return taskRepository.findAll();
        }
    }
}
