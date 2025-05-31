package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.service.NotificationPreferencesService;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.UserRepository;

@RestController
@RequestMapping("/api/notifications/preferences")
public class NotificationPreferencesController {
    @Autowired
    private NotificationPreferencesService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public NotificationPreferences getPreferences(Authentication auth) {
        return service.getPreferencesForUser(auth.getName());
    }

    @PostMapping
    public NotificationPreferences savePreferences(@RequestBody NotificationPreferences prefs, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return service.savePreferences(prefs, user);
    }
}
