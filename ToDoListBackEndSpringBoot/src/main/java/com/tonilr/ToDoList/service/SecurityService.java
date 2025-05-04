package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    @Autowired
    private UserService userService;

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public boolean isOwner(Long userId) {
        String currentUsername = getCurrentUsername();
        return userService.findByUsername(currentUsername).getId().equals(userId);
    }
}
