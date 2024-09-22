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
@Table(name = "Reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminder_id;

    private LocalDateTime reminderTime;  // Fecha y hora del recordatorio
    private String message;  // Mensaje del recordatorio
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id",nullable = true)
    private Task task;  // Tarea a la que está asociado el recordatorio
    
    // Getters y Setters
    public Long getReminder_Id() {
        return reminder_id;
    }

    public void setReminder_Id(Long reminder_id) {
        this.reminder_id = reminder_id;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
