package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service class for handling security-related operations.
 * Provides utility methods to access current user information
 * and verify user ownership of resources.
 */
@Service
public class SecurityService {
    @Autowired
    private UserService userService;

    /**
     * Retrieves the username of the currently authenticated user.
     * @return Username of the current user
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Verifies if the current user is the owner of a specific resource.
     * @param userId ID of the user to check ownership against
     * @return true if current user owns the resource, false otherwise
     */
    public boolean isOwner(Long userId) {
        String currentUsername = getCurrentUsername();
        return userService.findByUsername(currentUsername).getId().equals(userId);
    }
}
