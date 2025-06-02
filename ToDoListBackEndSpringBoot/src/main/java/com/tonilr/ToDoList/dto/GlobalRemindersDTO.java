package com.tonilr.ToDoList.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GlobalRemindersDTO {
    private boolean dueDateReminder;
    private String dueDateReminderTime;
    private boolean followUpReminder;
    private Integer followUpDays;
    private boolean dailySummary;
    private String dailySummaryTime;
    private boolean weeklySummary;
    private String weeklySummaryDay;
    private String weeklySummaryTime;
    private Integer minPriority;
    private boolean weekendNotifications;

    // Getters y Setters
    public boolean isDueDateReminder() { return dueDateReminder; }
    public void setDueDateReminder(boolean dueDateReminder) { this.dueDateReminder = dueDateReminder; }
    
    public String getDueDateReminderTime() { return dueDateReminderTime; }
    public void setDueDateReminderTime(String dueDateReminderTime) { this.dueDateReminderTime = dueDateReminderTime; }
    
    public boolean isFollowUpReminder() { return followUpReminder; }
    public void setFollowUpReminder(boolean followUpReminder) { this.followUpReminder = followUpReminder; }
    
    public Integer getFollowUpDays() { return followUpDays; }
    public void setFollowUpDays(Integer followUpDays) { this.followUpDays = followUpDays; }
    
    public boolean isDailySummary() { return dailySummary; }
    public void setDailySummary(boolean dailySummary) { this.dailySummary = dailySummary; }
    
    public String getDailySummaryTime() { return dailySummaryTime; }
    public void setDailySummaryTime(String dailySummaryTime) { this.dailySummaryTime = dailySummaryTime; }
    
    public boolean isWeeklySummary() { return weeklySummary; }
    public void setWeeklySummary(boolean weeklySummary) { this.weeklySummary = weeklySummary; }
    
    public String getWeeklySummaryDay() { return weeklySummaryDay; }
    public void setWeeklySummaryDay(String weeklySummaryDay) { this.weeklySummaryDay = weeklySummaryDay; }
    
    public String getWeeklySummaryTime() { return weeklySummaryTime; }
    public void setWeeklySummaryTime(String weeklySummaryTime) { this.weeklySummaryTime = weeklySummaryTime; }
    
    public Integer getMinPriority() { return minPriority; }
    public void setMinPriority(Integer minPriority) { this.minPriority = minPriority; }
    
    public boolean isWeekendNotifications() { return weekendNotifications; }
    public void setWeekendNotifications(boolean weekendNotifications) { this.weekendNotifications = weekendNotifications; }
}
