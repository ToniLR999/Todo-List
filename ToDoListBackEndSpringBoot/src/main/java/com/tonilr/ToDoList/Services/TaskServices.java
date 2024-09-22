package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Task;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.TaskRepo;

@Service
public class TaskServices {

	@Autowired
	private final TaskRepo taskRepo;

	public TaskServices(TaskRepo taskRepo) {
		this.taskRepo = taskRepo;

	}

	public Task addTask(Task ejercicio) {
		return taskRepo.save(ejercicio);
	}

	public List<Task> findAllTasks() {
		return taskRepo.findAll();
	}

	public Task updateTask(Task ejercicio) {
		return taskRepo.save(ejercicio);
	}

	public Task findTaskById(Long id) {
		return taskRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Task by id " + id + " was not found"));

	}

	public void deleteTask(Long id) {
		taskRepo.deleteById(id);
	}
}
