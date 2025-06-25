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

    @CacheEvict(value = {"tasks", "taskCounts", "userStats"}, allEntries = true)
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

    @Cacheable(value = "tasks", key = "'user_' + #username + '_all'")
    public List<CacheableTaskDTO> getUserTasks(String username) {
        System.out.println("OBTENIENDO TODAS LAS TAREAS DESDE BASE DE DATOS para usuario: " + username);
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
    
    @Cacheable(value = "tasks", key = "'user_' + #username + '_status_' + #showCompleted")
    public List<CacheableTaskDTO> getUserTasksByStatus(String username, boolean showCompleted) {
        System.out.println("OBTENIENDO TAREAS POR ESTADO DESDE BASE DE DATOS para usuario: " + username + ", completed: " + showCompleted);
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

    @Cacheable(value = "tasks", key = "'user_' + #username + '_priority_' + #priority")
    public List<CacheableTaskDTO> getUserTasksByPriority(String username, int priority) {
        System.out.println("OBTENIENDO TAREAS POR PRIORIDAD DESDE BASE DE DATOS para usuario: " + username + ", prioridad: " + priority);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndPriority(user, priority);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

    @Cacheable(value = "tasks", key = "'user_' + #username + '_duedate_' + #dueDate")
    public List<TaskDTO> getUserTasksByDueDate(String username, Date dueDate) {
        System.out.println("OBTENIENDO TAREAS POR FECHA DESDE BASE DE DATOS para usuario: " + username + ", fecha: " + dueDate);
        User user = userService.findByUsername(username);
        LocalDateTime localDateTime = dueDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
        return taskRepository.findByAssignedToAndDueDateBefore(user, localDateTime)
            .stream()
            .map(dtoMapper::toTaskDTO)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "tasks", key = "'user_' + #username + '_search_' + #title")
    public List<CacheableTaskDTO> searchUserTasksByTitle(String username, String title) {
        System.out.println("BUSCANDO TAREAS POR TÍTULO DESDE BASE DE DATOS para usuario: " + username + ", título: " + title);
        User user = userService.findByUsername(username);
        List<Task> tasks = taskRepository.findByAssignedToAndTitleContainingIgnoreCase(user, title);
        
        return tasks.stream()
            .map(task -> {
                TaskDTO dto = dtoMapper.toTaskDTO(task);
                return new CacheableTaskDTO(dto);
            })
            .collect(Collectors.toList());
    }

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

    @Cacheable(value = "tasks", key = "'task_' + #taskId")
    public TaskDTO getTaskDetails(Long taskId, String username) {
        System.out.println("OBTENIENDO DETALLES DE TAREA DESDE BASE DE DATOS para ID: " + taskId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + taskId));
        
        if (!securityService.isOwner(task.getAssignedTo().getId())) {
            throw new UnauthorizedException("No tienes permiso para ver esta tarea");
        }
        
        return dtoMapper.toTaskDTO(task);
    }

    @Cacheable(value = "tasks", key = "'user_' + #username + '_filtered_' + #search + '_' + #completed + '_' + #priority + '_' + #dateFilter + '_' + #taskListId")
    public List<CacheableTaskDTO> getFilteredTasks(String search, Boolean completed, String priority, String dateFilter, String username, Long taskListId) {
        System.out.println("OBTENIENDO TAREAS FILTRADAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        
        List<Task> tasks;
        if (taskListId != null) {
            tasks = taskRepository.findByTaskListId(taskListId);
        } else {
            tasks = taskRepository.findByAssignedTo(user);
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

    @Cacheable(value = "tasks", key = "'user_' + #username + '_list_' + #listId")
    public List<CacheableTaskDTO> getTasksByList(Long listId, String username) {
        System.out.println("OBTENIENDO TAREAS DE LISTA DESDE BASE DE DATOS para usuario: " + username + ", lista: " + listId);
        List<Task> tasks = taskRepository.findByTaskListId(listId);
        
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

    @Cacheable(value = "tasks", key = "#listId != null ? 'list_' + #listId : 'all'")
    public List<Task> getTasks(Long listId) {
        System.out.println("OBTENIENDO TAREAS DESDE BASE DE DATOS para listId: " + listId);
        if (listId != null) {
            return taskRepository.findByTaskListId(listId);
        } else {
            return taskRepository.findAll();
        }
    }
}
