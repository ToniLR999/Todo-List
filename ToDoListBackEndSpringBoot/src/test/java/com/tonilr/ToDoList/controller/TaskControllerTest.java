package com.tonilr.ToDoList.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.dto.CacheableTaskDTO;
import com.tonilr.ToDoList.service.TaskService;
import com.tonilr.ToDoList.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;
    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
        
        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setCompleted(false);
        testTaskDTO.setPriority(1);
        testTaskDTO.setCreatedAt(new Date());
    }

    @Test
    void getTasks_Success() throws Exception {
        when(securityService.getCurrentUsername()).thenReturn("testuser");
        when(taskService.getUserTasksByStatus(anyString(), any(Boolean.class)))
            .thenReturn(Arrays.asList(new CacheableTaskDTO(testTaskDTO)));

        mockMvc.perform(get("/api/tasks"))  
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void createTask_Success() throws Exception {
        when(securityService.getCurrentUsername()).thenReturn("testuser");
        when(taskService.createTask(any(TaskDTO.class), anyString())).thenReturn(testTaskDTO);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void updateTask_Success() throws Exception {
        when(taskService.updateTask(any(Long.class), any(TaskDTO.class))).thenReturn(testTaskDTO);

        mockMvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void deleteTask_Success() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isOk());
    }
}