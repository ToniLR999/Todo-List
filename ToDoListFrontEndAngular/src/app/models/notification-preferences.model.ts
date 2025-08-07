export interface NotificationPreferences {
  // Basic identification
  id?: number;
  email: string;
  notificationType?: string;
  
  // Due date reminders
  dueDateReminder: boolean;
  dueDateReminderTime?: string;
  
  // Follow-up reminders
  followUpReminder: boolean;
  followUpDays?: number;
  
  // Summary notifications
  dailySummary: boolean;
  dailySummaryTime?: string;
  weeklySummary: boolean;
  weeklySummaryDay?: string;
  weeklySummaryTime?: string;
  
  // General configuration
  minPriority?: number;
  weekendNotifications: boolean;
  
  // Legacy field (kept for backward compatibility)
  dailyReminders: boolean;
}

/**
 * DTO for updating notification preferences.
 * Used when sending data to the backend.
 */
export interface NotificationPreferencesUpdateDto {
  email: string;
  notificationType?: string;
  dueDateReminder: boolean;
  dueDateReminderTime?: string;
  followUpReminder: boolean;
  followUpDays?: number;
  dailySummary: boolean;
  dailySummaryTime?: string;
  weeklySummary: boolean;
  weeklySummaryDay?: string;
  weeklySummaryTime?: string;
  minPriority?: number;
  weekendNotifications: boolean;
  dailyReminders: boolean;
}

/**
 * Default notification preferences for new users.
 */
export const DEFAULT_NOTIFICATION_PREFERENCES: NotificationPreferences = {
  email: '',
  dueDateReminder: true,
  dueDateReminderTime: '09:00',
  followUpReminder: false,
  followUpDays: 1,
  dailySummary: false,
  dailySummaryTime: '08:00',
  weeklySummary: false,
  weeklySummaryDay: 'monday',
  weeklySummaryTime: '09:00',
  minPriority: 1,
  weekendNotifications: true,
  dailyReminders: false
};