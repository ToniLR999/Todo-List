package com.tonilr.ToDoList.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notif_prefs_user", columnList = "user_id"),
    @Index(name = "idx_notif_prefs_type", columnList = "notification_type")
})
@Data
@ToString
public class NotificationPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String email;
    private String notificationType;
    
    // Recordatorios de vencimiento
    private boolean dueDateReminder;
    private String dueDateReminderTime;
    
    // Recordatorios de seguimiento
    private boolean followUpReminder;
    private Integer followUpDays;
    
    // Resúmenes
    private boolean dailySummary;
    private String dailySummaryTime;
    private boolean weeklySummary;
    private String weeklySummaryDay;
    private String weeklySummaryTime;
    
    // Configuración general
    private Integer minPriority;
    private boolean weekendNotifications;

    @Column(name = "daily_reminders")
    private boolean dailyReminders = false;  // Valor por defecto

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

    public String getDueDateReminderTime() {
        return dueDateReminderTime;
    }

    public void setDueDateReminderTime(String dueDateReminderTime) {
        this.dueDateReminderTime = dueDateReminderTime;
    }

    public Integer getFollowUpDays() {
        return followUpDays;
    }

    public void setFollowUpDays(Integer followUpDays) {
        this.followUpDays = followUpDays;
    }

    public String getDailySummaryTime() {
        return dailySummaryTime;
    }

    public void setDailySummaryTime(String dailySummaryTime) {
        this.dailySummaryTime = dailySummaryTime;
    }

    public String getWeeklySummaryDay() {
        return weeklySummaryDay;
    }

    public void setWeeklySummaryDay(String weeklySummaryDay) {
        this.weeklySummaryDay = weeklySummaryDay;
    }

    public String getWeeklySummaryTime() {
        return weeklySummaryTime;
    }

    public void setWeeklySummaryTime(String weeklySummaryTime) {
        this.weeklySummaryTime = weeklySummaryTime;
    }

    public Integer getMinPriority() {
        return minPriority;
    }

    public void setMinPriority(Integer minPriority) {
        this.minPriority = minPriority;
    }

    public boolean isDueDateReminder() {
        return dueDateReminder;
    }

    public void setDueDateReminder(boolean dueDateReminder) {
        this.dueDateReminder = dueDateReminder;
    }

    public boolean isFollowUpReminder() {
        return followUpReminder;
    }

    public void setFollowUpReminder(boolean followUpReminder) {
        this.followUpReminder = followUpReminder;
    }

    public boolean isDailySummary() {
        return dailySummary;
    }

    public void setDailySummary(boolean dailySummary) {
        this.dailySummary = dailySummary;
    }

    public boolean isWeeklySummary() {
        return weeklySummary;
    }

    public void setWeeklySummary(boolean weeklySummary) {
        this.weeklySummary = weeklySummary;
    }

    public boolean isWeekendNotifications() {
        return weekendNotifications;
    }

    public void setWeekendNotifications(boolean weekendNotifications) {
        this.weekendNotifications = weekendNotifications;
    }

    public boolean isDailyReminders() {
        return dailyReminders;
    }

    public void setDailyReminders(boolean dailyReminders) {
        this.dailyReminders = dailyReminders;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
