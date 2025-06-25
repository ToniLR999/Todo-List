package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.CacheableTaskDTO;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CacheService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserService userService;

    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_total'")
    public Long getTaskCount(String username) {
        System.out.println("OBTENIENDO CONTEO DE TAREAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedTo(user);
    }

    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_completed'")
    public Long getCompletedTaskCount(String username) {
        System.out.println("OBTENIENDO CONTEO DE TAREAS COMPLETADAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedToAndCompleted(user, true);
    }

    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_pending'")
    public Long getPendingTaskCount(String username) {
        System.out.println("OBTENIENDO CONTEO DE TAREAS PENDIENTES DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedToAndCompleted(user, false);
    }

    @Cacheable(value = "userStats", key = "'user_' + #username + '_stats'")
    public String getUserStats(String username) {
        System.out.println("OBTENIENDO ESTADÍSTICAS DESDE BASE DE DATOS para usuario: " + username);
        Long total = getTaskCount(username);
        Long completed = getCompletedTaskCount(username);
        Long pending = getPendingTaskCount(username);
        
        return String.format("{\"totalTasks\": %d, \"completedTasks\": %d, \"pendingTasks\": %d}", 
                           total, completed, pending);
    }

    @CacheEvict(value = {"taskCounts", "userStats"}, allEntries = true)
    public void evictUserCache(String username) {
        System.out.println("EVICTANDO CACHÉ para usuario: " + username);
    }

    @CacheEvict(value = {"taskCounts", "userStats", "tasks", "taskLists", "users"}, allEntries = true)
    public void evictAllCache() {
        System.out.println("EVICTANDO TODO EL CACHÉ");
    }
}