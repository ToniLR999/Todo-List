package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.tonilr.ToDoList.dto.NotificationPreferencesDTO;
import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.service.NotificationPreferencesService;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * REST controller for managing user notification preferences.
 * Provides endpoints to get and update notification settings for the authenticated user.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationPreferencesService service;

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the notification preferences for the authenticated user.
     * @param userDetails The authenticated user's details
     * @return The user's notification preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferencesDTO> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(service.getPreferencesForUser(user.getUsername()));
    }

    /**
     * Updates the notification preferences for the authenticated user.
     * @param userDetails The authenticated user's details
     * @param preferences The new notification preferences
     * @return The updated notification preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferencesDTO> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody NotificationPreferencesDTO preferences) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(service.savePreferences(preferences, user));
    }
}
