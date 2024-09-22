package com.tonilr.ToDoList.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TaskHistorical")
public class TaskHistorical {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long taskHistorical_id;

	    private String action;   // Ejemplo: "CREATED", "UPDATED", "DELETED"
	    private LocalDateTime timestamp;

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
}
