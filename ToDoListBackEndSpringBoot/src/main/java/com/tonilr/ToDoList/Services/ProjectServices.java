package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Project;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.ProjectRepo;

@Service
public class ProjectServices {

	@Autowired
	private final ProjectRepo projectRepo;

	public ProjectServices(ProjectRepo projectRepo) {
		this.projectRepo = projectRepo;

	}

	public Project addProject(Project project) {
		return projectRepo.save(project);
	}

	public List<Project> findAllProjects() {
		return projectRepo.findAll();
	}

	public Project updateProject(Project project) {
		return projectRepo.save(project);
	}

	public Project findProjectById(Long id) {
		return projectRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Project by id " + id + " was not found"));

	}

	public void deleteProject(Long id) {
		projectRepo.deleteById(id);
	}
}
