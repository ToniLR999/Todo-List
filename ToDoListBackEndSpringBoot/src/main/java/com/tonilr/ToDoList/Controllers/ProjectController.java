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
import com.tonilr.ToDoList.Entities.Project;
import com.tonilr.ToDoList.Services.ProjectServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/project")
public class ProjectController {
	
	@Autowired
	private final ProjectServices projectService;

	
	public ProjectController(ProjectServices projectService) {
		this.projectService = projectService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Project>> getAllProjects() {
		List<Project> projects = projectService.findAllProjects();
		return new ResponseEntity<>(projects, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<Project> getProjectById(@PathVariable("id") Long id) {
		Project project = projectService.findProjectById(id);
		return new ResponseEntity<>(project, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<Project> addProject(@RequestBody Project project) {
		Project newProject = projectService.addProject(project);
		return new ResponseEntity<>(newProject, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<Project> updateProject(@RequestBody Project project) {
		Project updateProject = projectService.updateProject(project);
		return new ResponseEntity<>(updateProject, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteProject(@PathVariable("id") Long id) {
		projectService.deleteProject(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
