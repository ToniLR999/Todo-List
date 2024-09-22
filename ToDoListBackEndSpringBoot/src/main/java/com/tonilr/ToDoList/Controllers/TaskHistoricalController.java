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
import com.tonilr.ToDoList.Entities.TaskHistorical;
import com.tonilr.ToDoList.Services.TaskHistoricalServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/taskhistorical")
public class TaskHistoricalController {

	@Autowired
	private final TaskHistoricalServices taskHistoricalService;

	
	public TaskHistoricalController(TaskHistoricalServices taskHistoricalService) {
		this.taskHistoricalService = taskHistoricalService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<TaskHistorical>> getAllTaskHistoricals() {
		List<TaskHistorical> taskHistoricals = taskHistoricalService.findAllTaskHistoricals();
		return new ResponseEntity<>(taskHistoricals, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<TaskHistorical> getTaskHistoricalById(@PathVariable("id") Long id) {
		TaskHistorical taskHistorical = taskHistoricalService.findTaskHistoricalById(id);
		return new ResponseEntity<>(taskHistorical, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<TaskHistorical> addTaskHistorical(@RequestBody TaskHistorical taskHistorical) {
		TaskHistorical newTaskHistorical = taskHistoricalService.addTaskHistorical(taskHistorical);
		return new ResponseEntity<>(newTaskHistorical, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<TaskHistorical> updateRutina(@RequestBody TaskHistorical taskHistorical) {
		TaskHistorical updateTaskHistorical = taskHistoricalService.updateTaskHistorical(taskHistorical);
		return new ResponseEntity<>(updateTaskHistorical, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteTaskHistorical(@PathVariable("id") Long id) {
		taskHistoricalService.deleteTaskHistorical(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
