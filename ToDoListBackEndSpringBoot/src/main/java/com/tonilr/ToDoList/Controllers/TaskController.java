package com.tonilr.ToDoList.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tonilr.MyRoutines.entitys.Rutina;
import com.tonilr.MyRoutines.services.RutinaServices;
import com.tonilr.ToDoList.Entities.Task;
import com.tonilr.ToDoList.Services.TaskServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/task")
public class TaskController {
	
	
	@Autowired
	private final TaskServices taskService;

	
	public TaskController(TaskServices taskService) {
		this.taskService = taskService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Task>> getAllTasks() {
		List<Task> tasks = taskService.findAllTasks();
		return new ResponseEntity<>(tasks, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
		Task rutina = taskService.findTaskById(id);
		return new ResponseEntity<>(rutina, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<Task> addTask(@RequestBody Task task) {
		Task newTask = taskService.addTask(task);
		return new ResponseEntity<>(newTask, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<Task> updateTask(@RequestBody Task task) {
		Task updateTask = taskService.updateTask(task);
		return new ResponseEntity<>(updateTask, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteTask(@PathVariable("id") Long id) {
		taskService.deleteTask(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}


}
