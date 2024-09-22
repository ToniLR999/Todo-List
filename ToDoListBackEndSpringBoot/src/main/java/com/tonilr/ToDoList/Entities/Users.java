package com.tonilr.ToDoList.Entities;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "Users")
public class Users {


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, updatable = false)
	private Long user_Id;
	@Column(nullable = false, updatable = true)
	private String username;
	@Column(nullable = false, updatable = true)
	private String email;
	@Column(nullable = false, updatable = true)
	private String password;
	@Column(nullable = false, updatable = true)
	private Date register_date;
	
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id",nullable = true)
    private Role role;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<Task>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<List> lists = new HashSet<List>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Project> projects = new HashSet<Project>();
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> sharedProjects = new HashSet<Project>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TaskHistorical> tasksHistoricals = new HashSet<TaskHistorical>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<Comment>();
    
	public Users() {
	}

	public Users(String username, String email, String password) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public Long getUser_Id() {
		return user_Id;
	}

	public void setUser_Id(Long user_Id) {
		this.user_Id = user_Id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Date getRegisterDate() {
		return register_date;
	}

	public void setPassword(Date register_date) {
		this.register_date = register_date;
	}
	
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<List> getLists() {
        return lists;
    }

    public void setTodoLists(Set<List> lists) {
        this.lists = lists;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public Set<Project> getSharedProjects() {
        return sharedProjects;
    }

    public void setSharedProjects(Set<Project> sharedProjects) {
        this.sharedProjects = sharedProjects;
    }

    public Set<TaskHistorical> getActivityLogs() {
        return tasksHistoricals;
    }

    public void setActivityLogs(Set<TaskHistorical> tasksHistoricals) {
        this.tasksHistoricals = tasksHistoricals;
    }
}
