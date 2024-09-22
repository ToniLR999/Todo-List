package com.tonilr.ToDoList.Entities;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Tag")
public class Tag {

	
	  	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String name;  // Nombre de la etiqueta, como "@Alta", "@Trabajo", etc.

	    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	    private Set<Task> tasks;  // Lista de tareas asociadas a esta etiqueta

	    // Getters y Setters
	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    public Set<Task> getTasks() {
	        return tasks;
	    }

	    public void setTasks(Set<Task> tasks) {
	        this.tasks = tasks;
	    }
}
