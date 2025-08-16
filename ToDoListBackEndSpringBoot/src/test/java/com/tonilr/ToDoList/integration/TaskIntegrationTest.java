package com.tonilr.ToDoList.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonilr.ToDoList.dto.TaskDTO;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;
import com.tonilr.ToDoList.repository.UserRepository;
import com.tonilr.ToDoList.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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

    @MockBean
    private SecurityService securityService;

    private MockMvc mockMvc;
    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // 1. Configurar el mock del SecurityService PRIMERO
        when(securityService.getCurrentUsername()).thenReturn("integrationtest");
        
        // 2. Crear MockMvc DESPUÉS del mock
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 3. ESPERAR a que Spring termine de inicializar la base de datos
        // (Spring Boot hace esto automáticamente, pero necesitamos asegurarnos)
        
        // 4. Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("integrationtest");
        testUser.setEmail("integration@test.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);
        
        // 5. Verificar que el usuario se guardó correctamente
        assertNotNull(testUser.getId());
        
        // 6. Crear tarea de prueba
        testTask = new Task();
        testTask.setTitle("Integration Test Task");
        testTask.setDescription("Test Description");
        testTask.setPriority(1);
        testTask.setCompleted(false);
        testTask.setAssignedTo(testUser);
        testTask.setUser(testUser);
        testTask = taskRepository.save(testTask);
        
        // 7. Verificar que la tarea se guardó correctamente
        assertNotNull(testTask.getId());
        
        // 8. Mockear el método isOwner para que devuelva true para el usuario de prueba
        when(securityService.isOwner(testUser.getId())).thenReturn(true);
    }

    @Test
    void createTask_Integration_Success() throws Exception {
        // Arrange
        TaskDTO newTaskDTO = new TaskDTO();
        newTaskDTO.setTitle("Nueva Tarea de Integración");
        newTaskDTO.setDescription("Descripción de la nueva tarea");
        newTaskDTO.setPriority(2);
        newTaskDTO.setCompleted(false);
        newTaskDTO.setAssignedToId(testUser.getId());
        
        // Debug: Imprimir el DTO que se está enviando
        System.out.println("🔍 DEBUG - DTO enviado:");
        System.out.println("  - Title: " + newTaskDTO.getTitle());
        System.out.println("  - Description: " + newTaskDTO.getDescription());
        System.out.println("  - Priority: " + newTaskDTO.getPriority());
        System.out.println("  - Completed: " + newTaskDTO.isCompleted());
        System.out.println("  - AssignedToId: " + newTaskDTO.getAssignedToId());
        System.out.println("  - UserId: " + newTaskDTO.getUserId());
        
        // Debug: Verificar que el usuario existe
        System.out.println(" DEBUG - Usuario en BD:");
        System.out.println("  - ID: " + testUser.getId());
        System.out.println("  - Username: " + testUser.getUsername());
        
        // Act & Assert con logging detallado
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTaskDTO)))
                .andDo(print()) // Esto imprimirá la respuesta completa
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Nueva Tarea de Integración"))
                .andExpect(jsonPath("$.priority").value(2));

        // Verificar que se guardó en la base de datos
        assertTrue(taskRepository.findByAssignedTo(testUser).size() > 1);
    }

    @Test
    void getTasks_Integration_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .param("showCompleted", "false"))
                .andDo(print()) // Agregar logging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Integration Test Task"));
    }

    @Test
    void updateTask_Integration_Success() throws Exception {
        // Arrange
        TaskDTO updateTaskDTO = new TaskDTO();
        updateTaskDTO.setTitle("Updated Integration Task");
        updateTaskDTO.setCompleted(true);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTaskDTO)))
                .andDo(print()) // Agregar logging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteTask_Integration_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tasks/" + testTask.getId()))
                .andDo(print()) // Agregar logging
                .andExpect(status().isOk());

        // Verificar que se eliminó de la base de datos
        assertFalse(taskRepository.findById(testTask.getId()).isPresent());
    }
}