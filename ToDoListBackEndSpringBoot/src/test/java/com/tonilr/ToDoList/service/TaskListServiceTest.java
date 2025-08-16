package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.DTOMapper;
import com.tonilr.ToDoList.dto.TaskListDTO;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.exception.UnauthorizedException;
import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private DTOMapper dtoMapper;
    
    @Mock
    private SecurityService securityService;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private TaskListService taskListService;

    private User testUser;
    private TaskList testTaskList;
    private TaskListDTO testTaskListDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testTaskList = new TaskList();
        testTaskList.setId(1L);
        testTaskList.setName("Test List");
        testTaskList.setDescription("Test Description");
        testTaskList.setOwner(testUser);

        testTaskListDTO = new TaskListDTO();
        testTaskListDTO.setId(1L);
        testTaskListDTO.setName("Test List");
        testTaskListDTO.setDescription("Test Description");
    }

    @Test
    void createTaskList_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(dtoMapper.toTaskList(testTaskListDTO)).thenReturn(testTaskList);
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);
        when(dtoMapper.toTaskListDTO(testTaskList)).thenReturn(testTaskListDTO);
        doNothing().when(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskListDTO result = taskListService.createTaskList(testTaskListDTO, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("Test List", result.getName());
        verify(taskListRepository).save(any(TaskList.class));
        verify(emailService).sendSimpleEmail(anyString(), anyString(), anyString());
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void getUserTaskLists_Success() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(taskListRepository.findByOwnerIdWithTasks(testUser.getId())).thenReturn(Arrays.asList(testTaskList));
        when(dtoMapper.toTaskListDTO(testTaskList)).thenReturn(testTaskListDTO);

        // Act
        List<TaskListDTO> result = taskListService.getUserTaskLists("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test List", result.get(0).getName());
    }

    @Test
    void updateTaskList_Success() {
        // Arrange
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(securityService.isOwner(1L)).thenReturn(true);
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);
        when(dtoMapper.toTaskListDTO(testTaskList)).thenReturn(testTaskListDTO);
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        TaskListDTO result = taskListService.updateTaskList(1L, testTaskListDTO);

        // Assert
        assertNotNull(result);
        verify(taskListRepository).save(any(TaskList.class));
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void updateTaskList_Unauthorized() {
        // Arrange
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(securityService.isOwner(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            taskListService.updateTaskList(1L, testTaskListDTO);
        });
    }

    @Test
    void deleteTaskList_Success() {
        // Arrange
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(testTaskList));
        when(securityService.isOwner(1L)).thenReturn(true);
        doNothing().when(taskListRepository).delete(any(TaskList.class));
        doNothing().when(auditLogService).logAction(any(User.class), anyString(), anyString());

        // Act
        taskListService.deleteTaskList(1L);

        // Assert
        verify(taskListRepository).delete(any(TaskList.class));
        verify(auditLogService).logAction(any(User.class), anyString(), anyString());
    }

    @Test
    void deleteTaskList_NotFound() {
        // Arrange
        when(taskListRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskListService.deleteTaskList(1L);
        });
    }
}