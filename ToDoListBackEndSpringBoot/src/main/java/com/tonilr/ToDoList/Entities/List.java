package com.tonilr.ToDoList.Entities;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "List")
public class List {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long list_id;

    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id",nullable = true)
    private Project project;  // Proyecto al que pertenece la lista
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_Id",nullable = true)
    private Users user;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Task> tasks;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Users> sharedWith;  // Usuarios con los que se compartió la lista

 // Getters y Setters
    public Long getList_Id() {
        return list_id;
    }

    public void setList_Id(Long list_id) {
        this.list_id = list_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<Users> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(Set<Users> sharedWith) {
        this.sharedWith = sharedWith;
    }

    
}
