package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.NotificationPreferencesRepository;
import com.tonilr.ToDoList.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationPreferencesService {
    @Autowired
    private NotificationPreferencesRepository preferencesRepository;
    @Autowired
    private UserRepository userRepository;

    public NotificationPreferences getPreferencesForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return preferencesRepository.findByUser(user)
                .orElse(new NotificationPreferences()); // Devuelve por defecto si no hay
    }

    public NotificationPreferences savePreferencesForUser(String username, NotificationPreferences prefs) {
        User user = userRepository.findByUsername(username).orElseThrow();
        prefs.setUser(user);
        return preferencesRepository.save(prefs);
    }
}
