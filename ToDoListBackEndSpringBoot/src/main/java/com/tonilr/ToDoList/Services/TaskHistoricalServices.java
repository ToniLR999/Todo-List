package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.TaskHistorical;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.TaskHistoricalRepo;


@Service
public class TaskHistoricalServices {

	@Autowired
	private final TaskHistoricalRepo taskHistoricalRepo;

	public TaskHistoricalServices(TaskHistoricalRepo taskHistoricalRepo) {
		this.taskHistoricalRepo = taskHistoricalRepo;

	}

	public TaskHistorical addTaskHistorical(TaskHistorical ejercicio) {
		return taskHistoricalRepo.save(ejercicio);
	}

	public List<TaskHistorical> findAllTaskHistoricals() {
		return taskHistoricalRepo.findAll();
	}

	public TaskHistorical updateTaskHistorical(TaskHistorical ejercicio) {
		return taskHistoricalRepo.save(ejercicio);
	}

	public TaskHistorical findTaskHistoricalById(Long id) {
		return taskHistoricalRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("TaskHistorical by id " + id + " was not found"));

	}

	public void deleteTaskHistorical(Long id) {
		taskHistoricalRepo.deleteById(id);
	}
}
