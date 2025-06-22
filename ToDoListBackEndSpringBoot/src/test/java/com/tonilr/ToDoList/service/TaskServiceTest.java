package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.DTOMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.exception.UnauthorizedException;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskListRepository;
import com.tonilr.ToDoList.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private TaskListRepository taskListRepository;
    
    @Mock
    private DTOMapper dtoMapper;
    
    @Mock
    private SecurityService securityService;
    
    @Mock
    private CacheManager cacheManager;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;
    private TaskDTO testTaskDTO;
    private TaskList testTaskList;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTaskList = new TaskList();
        testTaskList.setId(1L);
        testTaskList.setName("Test List");
        testTaskList.setOwner(testUser);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setCompleted(false);
        testTask.setPriority(1);
        testTask.setUser(testUser);
        testTask.setAssignedTo(testUser);
        testTask.setTaskList(testTaskList);
        testTask.setCreatedAt(new Date());

        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setCompleted(false);
        testTaskDTO.setPriority(1);
        testTaskDTO.setTaskListId(1L);
    }

    @Test
    void createTask_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(dtoMapper.toTask(testTaskDTO)).thenReturn(testTask);
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(dtoMapper.toTaskDTO(testTask)).thenReturn(testTaskDTO);
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskDTO result = taskService.createTask(testTaskDTO, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
        verify(taskListRepository).findById(1L);
        verify(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void createTask_WithoutTaskList_Success() {
        // Arrange - Crear un TaskDTO sin taskListId
        TaskDTO taskDTOWithoutList = new TaskDTO();
        taskDTOWithoutList.setId(1L);
        taskDTOWithoutList.setTitle("Test Task");
        taskDTOWithoutList.setDescription("Test Description");
        taskDTOWithoutList.setCompleted(false);
        taskDTOWithoutList.setPriority(1);
        taskDTOWithoutList.setTaskListId(null); // Sin lista

        Task taskWithoutList = new Task();
        taskWithoutList.setId(1L);
        taskWithoutList.setTitle("Test Task");
        taskWithoutList.setDescription("Test Description");
        taskWithoutList.setCompleted(false);
        taskWithoutList.setPriority(1);
        taskWithoutList.setUser(testUser);
        taskWithoutList.setAssignedTo(testUser);
        taskWithoutList.setTaskList(null);
        taskWithoutList.setCreatedAt(new Date());

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(dtoMapper.toTask(taskDTOWithoutList)).thenReturn(taskWithoutList);
        when(taskRepository.save(any(Task.class))).thenReturn(taskWithoutList);
        when(dtoMapper.toTaskDTO(taskWithoutList)).thenReturn(taskDTOWithoutList);
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskDTO result = taskService.createTask(taskDTOWithoutList, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(any(Task.class));
        verify(taskListRepository, never()).findById(any());
        verify(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void createTask_WithTaskList_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(dtoMapper.toTask(testTaskDTO)).thenReturn(testTask);
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(dtoMapper.toTaskDTO(testTask)).thenReturn(testTaskDTO);
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskDTO result = taskService.createTask(testTaskDTO, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getTaskListId());
        verify(taskListRepository).findById(1L);
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void createTask_TaskListNotFound() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(dtoMapper.toTask(testTaskDTO)).thenReturn(testTask);
        when(taskListRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(testTaskDTO, "testuser");
        });
        verify(taskListRepository).findById(1L);
    }

    @Test
    void getUserTasks_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(taskRepository.findByAssignedTo(testUser)).thenReturn(Arrays.asList(testTask));
        when(dtoMapper.toTaskDTO(testTask)).thenReturn(testTaskDTO);

        // Act
        List<TaskDTO> result = taskService.getUserTasks("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void updateTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(securityService.isOwner(1L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(dtoMapper.toTaskDTO(testTask)).thenReturn(testTaskDTO);
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskDTO result = taskService.updateTask(1L, testTaskDTO);

        // Assert
        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void updateTask_Unauthorized() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(securityService.isOwner(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            taskService.updateTask(1L, testTaskDTO);
        });
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(securityService.isOwner(1L)).thenReturn(true);
        doNothing().when(taskRepository).delete(any(Task.class));
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).delete(any(Task.class));
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void deleteTask_NotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(1L);
        });
    }

    @Test
    void getTasksByList_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(taskRepository.findByTaskList(testTaskList)).thenReturn(Arrays.asList(testTask));
        when(dtoMapper.toTaskDTO(testTask)).thenReturn(testTaskDTO);

        // Act
        List<TaskDTO> result = taskService.getTasksByList(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void getTasksByList_NotFound() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(taskListRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTasksByList(1L, "testuser");
        });
    }
}