package com.tonilr.ToDoList.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminder_id;

    private LocalDateTime reminderTime;  // Fecha y hora del recordatorio
    private String message;  // Mensaje del recordatorio
    
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
}
