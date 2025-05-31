package com.tonilr.ToDoList.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class NotificationPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String notificationType;
    private String reminderTime;
    private String summaryFrequency;
    private String minPriority;
    private boolean dailyReminders;
    private boolean weeklySummary;

    @OneToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getSummaryFrequency() {
        return summaryFrequency;
    }

    public void setSummaryFrequency(String summaryFrequency) {
        this.summaryFrequency = summaryFrequency;
    }

    public String getMinPriority() {
        return minPriority;
    }

    public void setMinPriority(String minPriority) {
        this.minPriority = minPriority;
    }

    public boolean isDailyReminders() {
        return dailyReminders;
    }

    public void setDailyReminders(boolean dailyReminders) {
        this.dailyReminders = dailyReminders;
    }

    public boolean isWeeklySummary() {
        return weeklySummary;
    }

    public void setWeeklySummary(boolean weeklySummary) {
        this.weeklySummary = weeklySummary;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
