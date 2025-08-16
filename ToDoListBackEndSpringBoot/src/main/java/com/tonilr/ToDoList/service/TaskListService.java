package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.DTOMapper;
import com.tonilr.ToDoList.dto.TaskListDTO;
import com.tonilr.ToDoList.exception.BadRequestException;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.exception.UnauthorizedException;
import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

/**
 * Service class for managing task list operations.
 * Provides functionality to create, retrieve, update, delete, and search task lists
 * with proper authorization checks, caching, and audit logging.
 */
@Service
public class TaskListService {
    private static final Logger log = LoggerFactory.getLogger(TaskListService.class);

    @Autowired
    private TaskListRepository taskListRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    /**
     * Creates a new task list for the specified user.
     * @param taskListDTO Task list data to create
     * @param username Username of the list owner
     * @return Created task list DTO
     */
    @CacheEvict(value = {"taskLists", "tasks"}, allEntries = true)
    @Transactional
    public TaskListDTO createTaskList(TaskListDTO taskListDTO, String username) {
        User owner = userService.findByUsername(username);
        TaskList taskList = dtoMapper.toTaskList(taskListDTO);
        taskList.setOwner(owner);
        TaskList savedList = taskListRepository.save(taskList);

        // Notificación de nueva lista
        emailService.sendSimpleEmail(
            owner.getEmail(),
            "Nueva lista creada",
            "Has creado una nueva lista: " + taskList.getName()
        );

        auditLogService.logAction(owner, "CREAR_LISTA", "Lista creada: " + taskList.getName());
        return dtoMapper.toTaskListDTO(savedList);
    }

    /**
     * Retrieves all task lists for a specific user with caching.
     * @param username Username to get task lists for
     * @return List of task list DTOs
     */
    @Cacheable(value = "taskLists", key = "'user_' + #username")
    public List<TaskListDTO> getUserTaskLists(String username) {
        //log.info("🔍 ===== INICIO TaskListService.getUserTaskLists =====");
        //log.info(" Username recibido: {}", username);
        
        try {
            //log.info("🔍 Llamando a userService.findByUsername('{}')", username);
            User user = userService.findByUsername(username);
            //log.info(" Usuario encontrado: {}", user != null ? "SÍ" : "NO");
            
            if (user == null) {
                //log.warn("⚠️ Usuario no encontrado para username: {}", username);
                return Collections.emptyList();
            }
            
            //log.info(" Usuario ID: {}, Username: {}", user.getId(), user.getUsername());
            
            List<TaskList> taskLists = taskListRepository.findByOwnerIdWithTasks(user.getId());
            
            //log.info("✅ Listas encontradas en BD: {}", taskLists.size());
            
            if (!taskLists.isEmpty()) {
                log.info("📋 Primera lista en BD: ID={}, Name={}, Owner ID={}", 
                    taskLists.get(0).getId(), 
                    taskLists.get(0).getName(),
                    taskLists.get(0).getOwner() != null ? taskLists.get(0).getOwner().getId() : "NULL");
            }
            
            // Convertir a DTOs
            //log.info("🔍 Convirtiendo {} listas a DTOs", taskLists.size());
            List<TaskListDTO> dtos = taskLists.stream()
                .map(dtoMapper::toTaskListDTO)
                .collect(Collectors.toList());
            
            //log.info("✅ DTOs convertidos: {}", dtos.size());
            //log.info(" ===== FIN TaskListService.getUserTaskLists =====");
            
            return dtos;
            
        } catch (Exception e) {
            //log.error("❌ ===== ERROR EN TaskListService.getUserTaskLists =====");
            //log.error("❌ Username: {}", username);
            //log.error("❌ Error completo:", e);
            //log.error("❌ Stack trace:", e);
            return Collections.emptyList();
        }
    }

    /**
     * Updates an existing task list with authorization checks.
     * @param listId ID of the task list to update
     * @param taskListDetails Updated task list data
     * @return Updated task list DTO
     */
    @CacheEvict(value = {"taskLists", "tasks"}, allEntries = true)
    @Transactional
    public TaskListDTO updateTaskList(Long listId, TaskListDTO taskListDetails) {
        TaskList taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada con ID: " + listId));

        if (!securityService.isOwner(taskList.getOwner().getId())) {
            throw new UnauthorizedException("No tienes permiso para modificar esta lista");
        }

        if (taskListDetails.getName() == null || taskListDetails.getName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la lista no puede estar vacío");
        }

        taskList.setName(taskListDetails.getName());
        taskList.setDescription(taskListDetails.getDescription());
        
        TaskList updatedList = taskListRepository.save(taskList);
        auditLogService.logAction(taskList.getOwner(), "ACTUALIZAR_LISTA", "Lista actualizada: " + updatedList.getName());
        return dtoMapper.toTaskListDTO(updatedList);
    }

    /**
     * Deletes a task list with authorization checks.
     * @param listId ID of the task list to delete
     */
    @CacheEvict(value = {"taskLists", "tasks"}, allEntries = true)
    @Transactional
    public void deleteTaskList(Long listId) {
        TaskList taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada con ID: " + listId));
        if (!securityService.isOwner(taskList.getOwner().getId())) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta lista");
        }
        auditLogService.logAction(taskList.getOwner(), "ELIMINAR_LISTA", "Lista eliminada: " + taskList.getName());
        taskListRepository.delete(taskList);
    }

    /**
     * Searches task lists by name for a specific user with caching.
     * @param username Username to search task lists for
     * @param name Name to search for (case-insensitive)
     * @return List of matching task list DTOs
     */
    @Cacheable(value = "taskLists", key = "'user_' + #username + '_search_' + #name")
    public List<TaskListDTO> searchUserTaskListsByName(String username, String name) {
        User owner = userService.findByUsername(username);
        return taskListRepository.findByOwnerAndNameContainingIgnoreCase(owner, name)
            .stream()
            .map(dtoMapper::toTaskListDTO)
            .collect(Collectors.toList());
    }
}
