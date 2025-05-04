package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.TaskList;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TaskListService {
    @Autowired
    private TaskListRepository taskListRepository;
    
    @Autowired
    private UserService userService;

    @Transactional
    public TaskList createTaskList(TaskList taskList, String username) {
        User owner = userService.findByUsername(username);
        taskList.setOwner(owner);
        return taskListRepository.save(taskList);
    }

    public List<TaskList> getUserTaskLists(String username) {
        User owner = userService.findByUsername(username);
        return taskListRepository.findByOwner(owner);
    }

    @Transactional
    public TaskList updateTaskList(Long listId, TaskList taskListDetails) {
        TaskList taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
            
        taskList.setName(taskListDetails.getName());
        return taskListRepository.save(taskList);
    }

    @Transactional
    public void deleteTaskList(Long listId) {
        TaskList taskList = taskListRepository.findById(listId)
            .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
        taskListRepository.delete(taskList);
    }
}
