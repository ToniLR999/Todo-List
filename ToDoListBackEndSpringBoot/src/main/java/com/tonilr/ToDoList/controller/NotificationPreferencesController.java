package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.service.NotificationPreferencesService;

@RestController
@RequestMapping("/api/notifications/preferences")
public class NotificationPreferencesController {
    @Autowired
    private NotificationPreferencesService service;

    @GetMapping
    public NotificationPreferences getPreferences(Authentication auth) {
        return service.getPreferencesForUser(auth.getName());
    }

    @PostMapping
    public NotificationPreferences savePreferences(@RequestBody NotificationPreferences prefs, Authentication auth) {
        return service.savePreferencesForUser(auth.getName(), prefs);
    }
}
