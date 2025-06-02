package com.tonilr.ToDoList.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NotificationPreferencesDTO {
    private String email;
    private String notificationType;
    private GlobalRemindersDTO globalReminders;

    // Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    
    public GlobalRemindersDTO getGlobalReminders() { return globalReminders; }
    public void setGlobalReminders(GlobalRemindersDTO globalReminders) { this.globalReminders = globalReminders; }
}
