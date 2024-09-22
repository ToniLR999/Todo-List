package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.TaskList;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.TaskListRepo;

@Service
public class TaskListServices {
	
	@Autowired
	private final TaskListRepo listRepo;

	public TaskListServices(TaskListRepo listRepo) {
		this.listRepo = listRepo;

	}

	public TaskList addList(TaskList list) {
		return listRepo.save(list);
	}

	public List<TaskList> findAllLists() {
		return listRepo.findAll();
	}

	public TaskList updateList(TaskList list) {
		return listRepo.save(list);
	}

	public TaskList findListById(Long id) {
		return listRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("List by id " + id + " was not found"));

	}

	public void deleteList(Long id) {
		listRepo.deleteById(id);
	}

}
