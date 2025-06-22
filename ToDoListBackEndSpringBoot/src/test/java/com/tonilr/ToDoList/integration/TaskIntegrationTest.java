package com.tonilr.ToDoList.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;
import com.tonilr.ToDoList.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("integrationtest");
        testUser.setEmail("integration@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        // Crear tarea de prueba
        testTask = new Task();
        testTask.setTitle("Integration Test Task");
        testTask.setDescription("Test Description");
        testTask.setPriority(1);
        testTask.setCompleted(false);
        testTask.setAssignedTo(testUser);
        testTask.setUser(testUser);
        testTask = taskRepository.save(testTask);
    }

    @Test
    @WithMockUser(username = "integrationtest")
    void createTask_Integration_Success() throws Exception {
        // Arrange
        TaskDTO newTaskDTO = new TaskDTO();
        newTaskDTO.setTitle("New Integration Task");
        newTaskDTO.setDescription("New Description");
        newTaskDTO.setPriority(2);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Integration Task"))
                .andExpect(jsonPath("$.priority").value(2));

        // Verificar que se guardó en la base de datos
        assertTrue(taskRepository.findByAssignedTo(testUser).size() > 1);
    }

    @Test
    @WithMockUser(username = "integrationtest")
    void getTasks_Integration_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .param("showCompleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Integration Test Task"));
    }

    @Test
    @WithMockUser(username = "integrationtest")
    void updateTask_Integration_Success() throws Exception {
        // Arrange
        TaskDTO updateTaskDTO = new TaskDTO();
        updateTaskDTO.setTitle("Updated Integration Task");
        updateTaskDTO.setCompleted(true);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @WithMockUser(username = "integrationtest")
    void deleteTask_Integration_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tasks/" + testTask.getId()))
                .andExpect(status().isOk());

        // Verificar que se eliminó de la base de datos
        assertFalse(taskRepository.findById(testTask.getId()).isPresent());
    }
}