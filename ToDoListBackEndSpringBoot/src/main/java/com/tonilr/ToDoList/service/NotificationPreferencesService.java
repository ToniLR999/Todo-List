package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.dto.GlobalRemindersDTO;
import com.tonilr.ToDoList.dto.NotificationPreferencesDTO;
import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.NotificationPreferencesRepository;
import com.tonilr.ToDoList.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationPreferencesService {
    @Autowired
    private NotificationPreferencesRepository repository;
    @Autowired
    private UserRepository userRepository;

    public NotificationPreferencesDTO getPreferencesForUser(String username) {
        log.info("Obteniendo preferencias para usuario: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        log.info("Usuario encontrado con ID: {}", user.getId());
        
        NotificationPreferences preferences = repository.findByUser(user)
                .orElseGet(() -> {
                    log.info("No se encontraron preferencias, creando por defecto para usuario: {}", username);
                    return createDefaultPreferences(user);
                });
        log.info("Preferencias encontradas/creadas con ID: {}", preferences.getId());
        
        return convertToDTO(preferences);
    }

    public NotificationPreferencesDTO savePreferences(NotificationPreferencesDTO dto, User user) {
        log.info("Guardando preferencias para usuario: {}", user.getUsername());
        log.info("DTO recibido: {}", dto);
        
        NotificationPreferences preferences = repository.findByUser(user)
                .orElseGet(() -> {
                    log.info("No se encontraron preferencias existentes, creando nuevas para usuario: {}", user.getUsername());
                    return new NotificationPreferences();
                });
        
        log.info("Preferencias actuales: {}", preferences);
        
        // Actualizar campos básicos
        preferences.setUser(user);
        preferences.setEmail(dto.getEmail());
        preferences.setNotificationType(dto.getNotificationType());
        
        // Actualizar campos de globalReminders
        if (dto.getGlobalReminders() != null) {
            GlobalRemindersDTO global = dto.getGlobalReminders();
            log.info("Actualizando globalReminders: {}", global);
            
            preferences.setDueDateReminder(global.isDueDateReminder());
            preferences.setDueDateReminderTime(global.getDueDateReminderTime());
            preferences.setFollowUpReminder(global.isFollowUpReminder());
            preferences.setFollowUpDays(global.getFollowUpDays());
            preferences.setDailySummary(global.isDailySummary());
            preferences.setDailySummaryTime(global.getDailySummaryTime());
            preferences.setWeeklySummary(global.isWeeklySummary());
            preferences.setWeeklySummaryDay(global.getWeeklySummaryDay());
            preferences.setWeeklySummaryTime(global.getWeeklySummaryTime());
            preferences.setMinPriority(global.getMinPriority());
            preferences.setWeekendNotifications(global.isWeekendNotifications());
        }
        
        try {
            preferences = repository.save(preferences);
            log.info("Preferencias guardadas exitosamente con ID: {}", preferences.getId());
        } catch (Exception e) {
            log.error("Error al guardar preferencias: {}", e.getMessage(), e);
            throw e;
        }
        
        return convertToDTO(preferences);
    }

    private NotificationPreferencesDTO convertToDTO(NotificationPreferences preferences) {
        log.info("Convirtiendo preferencias a DTO: {}", preferences);
        NotificationPreferencesDTO dto = new NotificationPreferencesDTO();
        dto.setEmail(preferences.getEmail());
        dto.setNotificationType(preferences.getNotificationType());
        
        GlobalRemindersDTO global = new GlobalRemindersDTO();
        global.setDueDateReminder(preferences.isDueDateReminder());
        global.setDueDateReminderTime(preferences.getDueDateReminderTime());
        global.setFollowUpReminder(preferences.isFollowUpReminder());
        global.setFollowUpDays(preferences.getFollowUpDays());
        global.setDailySummary(preferences.isDailySummary());
        global.setDailySummaryTime(preferences.getDailySummaryTime());
        global.setWeeklySummary(preferences.isWeeklySummary());
        global.setWeeklySummaryDay(preferences.getWeeklySummaryDay());
        global.setWeeklySummaryTime(preferences.getWeeklySummaryTime());
        global.setMinPriority(preferences.getMinPriority());
        global.setWeekendNotifications(preferences.isWeekendNotifications());
        
        dto.setGlobalReminders(global);
        log.info("DTO convertido: {}", dto);
        return dto;
    }

    private NotificationPreferences createDefaultPreferences(User user) {
        log.info("Creando preferencias por defecto para usuario: {}", user.getUsername());
        NotificationPreferences preferences = new NotificationPreferences();
        preferences.setUser(user);
        preferences.setEmail(user.getEmail());
        preferences.setNotificationType("both");
        preferences.setDueDateReminder(true);
        preferences.setDueDateReminderTime("1d");
        preferences.setFollowUpReminder(true);
        preferences.setFollowUpDays(3);
        preferences.setDailySummary(true);
        preferences.setDailySummaryTime("09:00");
        preferences.setWeeklySummary(true);
        preferences.setWeeklySummaryDay("monday");
        preferences.setWeeklySummaryTime("10:00");
        preferences.setMinPriority(2);
        preferences.setWeekendNotifications(false);
        
        try {
            preferences = repository.save(preferences);
            log.info("Preferencias por defecto creadas con ID: {}", preferences.getId());
        } catch (Exception e) {
            log.error("Error al crear preferencias por defecto: {}", e.getMessage(), e);
            throw e;
        }
        
        return preferences;
    }
}
