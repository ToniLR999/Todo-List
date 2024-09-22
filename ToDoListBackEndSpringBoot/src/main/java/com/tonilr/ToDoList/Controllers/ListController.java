package com.tonilr.ToDoList.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tonilr.ToDoList.Entities.TaskList;
import com.tonilr.ToDoList.Services.TaskListServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/list")
public class ListController {
	
	@Autowired
	private final TaskListServices listService;

	
	public ListController(TaskListServices listService) {
		this.listService = listService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<TaskList>> getAllTaskLists() {
		List<TaskList> taskLists = listService.findAllLists();
		return new ResponseEntity<>(taskLists, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<TaskList> getTaskListById(@PathVariable("id") Long id) {
		TaskList taskList = listService.findListById(id);
		return new ResponseEntity<>(taskList, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<TaskList> addTaskList(@RequestBody TaskList taskList) {
		TaskList newTaskList = listService.addList(taskList);
		return new ResponseEntity<>(newTaskList, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<TaskList> updateTaskList(@RequestBody TaskList taskList) {
		TaskList updateTaskList = listService.updateList(taskList);
		return new ResponseEntity<>(updateTaskList, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteTaskList(@PathVariable("id") Long id) {
		listService.deleteList(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
