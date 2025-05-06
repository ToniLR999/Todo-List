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

@Service
public class TaskListService {
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

    public List<TaskListDTO> getUserTaskLists(String username) {
        User owner = userService.findByUsername(username);
        return taskListRepository.findByOwner(owner)
            .stream()
            .map(dtoMapper::toTaskListDTO)
            .collect(Collectors.toList());
    }

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
        TaskList updatedList = taskListRepository.save(taskList);
        auditLogService.logAction(taskList.getOwner(), "ACTUALIZAR_LISTA", "Lista actualizada: " + updatedList.getName());
        return dtoMapper.toTaskListDTO(updatedList);
    }

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
}
