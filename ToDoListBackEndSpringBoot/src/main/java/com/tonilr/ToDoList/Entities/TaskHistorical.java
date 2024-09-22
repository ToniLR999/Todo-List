package com.tonilr.ToDoList.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TaskHistorical")
public class TaskHistorical {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long taskHistorical_id;

	    private String action;   // Ejemplo: "CREATED", "UPDATED", "DELETED"
	    private LocalDateTime timestamp;
	    
	    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	    @JoinColumn(name = "user_Id",nullable = true)
	    private Users user;
	    
	    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	    @JoinColumn(name = "task_id",nullable = true)
	    private Task task;  // La tarea sobre la que se realizó la acción

	    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
	    @JoinColumn(name = "project_id",nullable = true)
	    private Project project;  // El proyecto sobre el que se realizó la acción

	    // Getters y Setters
	    public Long getTaskHistorical_Id() {
	        return taskHistorical_id;
	    }

	    public void setTaskHistorical_Id(Long taskHistorical_id) {
	        this.taskHistorical_id = taskHistorical_id;
	    }

	    public String getAction() {
	        return action;
	    }

	    public void setAction(String action) {
	        this.action = action;
	    }

	    public LocalDateTime getTimestamp() {
	        return timestamp;
	    }

	    public void setTimestamp(LocalDateTime timestamp) {
	        this.timestamp = timestamp;
	    }
	    
	    public Users getUser() {
	        return user;
	    }

	    public void setUser(Users user) {
	        this.user = user;
	    }
	    
	    public Task getTask() {
	        return task;
	    }

	    public void setTask(Task task) {
	        this.task = task;
	    }

	    public Project getProject() {
	        return project;
	    }

	    public void setProject(Project project) {
	        this.project = project;
	    }
}
