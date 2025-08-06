package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.UserDTO;
import com.tonilr.ToDoList.dto.UserRegistrationDTO;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

import com.tonilr.ToDoList.service.SecurityService;
import com.tonilr.ToDoList.dto.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * REST controller for user management and profile operations.
 * Provides endpoints for registration, profile retrieval, and profile updates.
 * All endpoints are secured and require authentication, except registration.
 */
@RestController
@RequestMapping("/api/auth/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DTOMapper dtoMapper;

    /**
     * Registers a new user.
     * @param userDTO User registration data
     * @return The created user profile or error message
     */
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO userDTO) {
        try {
            if (userDTO.getTimezone() == null || userDTO.getTimezone().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timezone is required");
            }
            var user = dtoMapper.toUser(userDTO);
            var newUser = userService.registerUser(user);
            return ResponseEntity.ok(dtoMapper.toUserDTO(newUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * @return The user profile or error message
     */
    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String username = securityService.getCurrentUsername();
            var user = userService.findByUsername(username);
            return ResponseEntity.ok(dtoMapper.toUserProfileDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the profile of the currently authenticated user.
     * @param userDetails New user details
     * @return The updated user profile or error message
     */
    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO userDetails) {
        try {
            String username = securityService.getCurrentUsername();
            var user = userService.findByUsername(username);
            user.setEmail(userDetails.getEmail());
            user.setTimezone(userDetails.getTimezone());
            var updatedUser = userService.updateUser(user.getId(), user);
            return ResponseEntity.ok(dtoMapper.toUserDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates the timezone of the currently authenticated user.
     * @param timezone New timezone
     * @param auth Authentication object
     * @return HTTP 200 if successful
     */
    @PutMapping("/users/timezone")
    public ResponseEntity<?> updateTimezone(@RequestParam String timezone, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        user.setTimezone(timezone);
        userService.save(user);
        return ResponseEntity.ok().build();
    }
}
