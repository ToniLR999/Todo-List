package com.tonilr.ToDoList.Entities;

import java.util.HashSet;
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
@Table(name = "Project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long project_id;

    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_Id",nullable = true)
    private Users user;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<List> lists;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Users> sharedWith = new HashSet<Users>();  // Usuarios con los que se compartió la lista
	
    // Getters y Setters
    public Long getProject_Id() {
        return project_id;
    }

    public void setProject_Id(Long project_id) {
        this.project_id = project_id;
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
    
    public Set<List> getLists() {
        return lists;
    }

    public void setLists(Set<List> lists) {
        this.lists = lists;
    }
    
    public Set<Users> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(Set<Users> sharedWith) {
        this.sharedWith = sharedWith;
    }
}
