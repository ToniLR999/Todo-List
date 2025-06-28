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

/**
 * Service class for managing application caching operations.
 * Provides cached access to frequently requested data like task counts and user statistics,
 * improving application performance by reducing database queries.
 */
@Service
public class CacheService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserService userService;

    /**
     * Retrieves the total number of tasks for a user with caching.
     * @param username The username to get task count for
     * @return Total number of tasks for the user
     */
    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_total'")
    public Long getTaskCount(String username) {
        // System.out.println("OBTENIENDO CONTEO DE TAREAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedTo(user);
    }

    /**
     * Retrieves the number of completed tasks for a user with caching.
     * @param username The username to get completed task count for
     * @return Number of completed tasks for the user
     */
    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_completed'")
    public Long getCompletedTaskCount(String username) {
        // System.out.println("OBTENIENDO CONTEO DE TAREAS COMPLETADAS DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedToAndCompleted(user, true);
    }

    /**
     * Retrieves the number of pending tasks for a user with caching.
     * @param username The username to get pending task count for
     * @return Number of pending tasks for the user
     */
    @Cacheable(value = "taskCounts", key = "'user_' + #username + '_pending'")
    public Long getPendingTaskCount(String username) {
        // System.out.println("OBTENIENDO CONTEO DE TAREAS PENDIENTES DESDE BASE DE DATOS para usuario: " + username);
        User user = userService.findByUsername(username);
        return taskRepository.countByAssignedToAndCompleted(user, false);
    }

    /**
     * Retrieves comprehensive user statistics with caching.
     * @param username The username to get statistics for
     * @return JSON string containing total, completed, and pending task counts
     */
    @Cacheable(value = "userStats", key = "'user_' + #username + '_stats'")
    public String getUserStats(String username) {
        // System.out.println("OBTENIENDO ESTADÍSTICAS DESDE BASE DE DATOS para usuario: " + username);
        Long total = getTaskCount(username);
        Long completed = getCompletedTaskCount(username);
        Long pending = getPendingTaskCount(username);
        
        return String.format("{\"totalTasks\": %d, \"completedTasks\": %d, \"pendingTasks\": %d}", 
                           total, completed, pending);
    }

    /**
     * Evicts all cache entries for a specific user.
     * @param username The username whose cache should be cleared
     */
    @CacheEvict(value = {"taskCounts", "userStats"}, allEntries = true)
    public void evictUserCache(String username) {
        // System.out.println("EVICTANDO CACHÉ para usuario: " + username);
    }

    /**
     * Evicts all cache entries across the entire application.
     * Used for maintenance or when cache consistency is compromised.
     */
    @CacheEvict(value = {"taskCounts", "userStats", "tasks", "taskLists", "users"}, allEntries = true)
    public void evictAllCache() {
        // System.out.println("EVICTANDO TODO EL CACHÉ");
    }
}