package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserService userService;

    @Transactional
    public Task createTask(Task task, String username) {
        User user = userService.findByUsername(username);
        task.setAssignedTo(user);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<Task> getUserTasks(String username) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedTo(user);
    }

    public List<Task> getUserTasksByStatus(String username, boolean completed) {
        User user = userService.findByUsername(username);
        return taskRepository.findByAssignedToAndCompleted(user, completed);
    }

    @Transactional
    public Task updateTask(Long taskId, Task taskDetails) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
            
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        taskRepository.delete(task);
    }
}
