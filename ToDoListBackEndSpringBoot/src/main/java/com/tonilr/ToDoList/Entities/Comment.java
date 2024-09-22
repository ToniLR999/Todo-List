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
@Table(name = "Comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comment_id;

    private String content;
    private LocalDateTime timestamp;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "task_id",nullable = true)
    private Task task;

    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_Id",nullable = true)
    private Users user;
    
    // Getters y Setters
    public Long getComment_Id() {
        return comment_id;
    }

    public void setComment_Id(Long comment_id) {
        this.comment_id = comment_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}

