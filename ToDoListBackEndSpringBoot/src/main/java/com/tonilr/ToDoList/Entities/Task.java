package com.tonilr.ToDoList.Entities;

import java.time.LocalDateTime;
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
@Table(name = "Task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long task_id;

    private String content;
    private boolean completed;
    
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_Id",nullable = true)
    private Users user; 
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "list_id",nullable = true)
    private List List;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Tag> tags;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Reminder> reminders;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<TaskHistorical> taskHistoricals;


    // Getters y Setters
    public Long getTask_Id() {
        return task_id;
    }

    public void setTask_Id(Long task_id) {
        this.task_id = task_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public Users getUser() {
        return user;
    }

    public void setAssignedUser(Users user) {
        this.user = user;
    }
    
    public List getList() {
        return List;
    }

    public void setList(List List) {
        this.List = List;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(Set<Reminder> reminders) {
        this.reminders = reminders;
    }

    public Set<TaskHistorical> getTaskHistoricals() {
        return taskHistoricals;
    }

    public void setTaskHistoricals(Set<TaskHistorical> taskHistoricals) {
        this.taskHistoricals = taskHistoricals;
    }


}
