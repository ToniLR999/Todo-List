package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.NotificationPreferencesRepository;
import com.tonilr.ToDoList.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationPreferencesService {
    @Autowired
    private NotificationPreferencesRepository repository;
    @Autowired
    private UserRepository userRepository;

    public NotificationPreferences getPreferencesForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return repository.findByUser(user)
                .orElse(new NotificationPreferences()); // Devuelve por defecto si no hay
    }

    public NotificationPreferences savePreferences(NotificationPreferences preferences, User user) {
        // Buscar preferencias existentes
        Optional<NotificationPreferences> existingPrefs = repository.findByUser(user);
        
        if (existingPrefs.isPresent()) {
            // Actualizar preferencias existentes
            NotificationPreferences currentPrefs = existingPrefs.get();
            currentPrefs.setEmail(preferences.getEmail());
            currentPrefs.setNotificationType(preferences.getNotificationType());
            currentPrefs.setReminderTime(preferences.getReminderTime());
            currentPrefs.setSummaryFrequency(preferences.getSummaryFrequency());
            currentPrefs.setMinPriority(preferences.getMinPriority());
            currentPrefs.setDailyReminders(preferences.isDailyReminders());
            currentPrefs.setWeeklySummary(preferences.isWeeklySummary());
            return repository.save(currentPrefs);
        } else {
            // Crear nuevas preferencias
            preferences.setUser(user);
            return repository.save(preferences);
        }
    }
}
