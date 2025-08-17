package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tonilr.ToDoList.dto.PasswordResetDTO;
import com.tonilr.ToDoList.dto.PasswordResetRequestDTO;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.security.JwtTokenProvider;
import com.tonilr.ToDoList.service.EmailService;
import com.tonilr.ToDoList.service.UserService;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;

/**
 * REST controller for password reset operations.
 * Provides endpoints for requesting a password reset and for resetting the password.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class PasswordResetController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Autowired
    public PasswordResetController(UserService userService, 
                                JwtTokenProvider tokenProvider,
                                EmailService emailService) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
    }

    /**
     * Endpoint to request a password reset.
     * Sends a password reset email with a token to the user's email address.
     * @param request Contains the user's email
     * @return Success message or error
     */
    @Operation(summary = "Request password reset")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequestDTO request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            String token = tokenProvider.generatePasswordResetToken(user.getUsername());
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return ResponseEntity.ok().body(Map.of("message", "Password reset email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to reset the user's password using a valid token.
     * @param request Contains the reset token and the new password
     * @return Success message or error
     */
    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO request) {
        try {
            String username = tokenProvider.getUsernameFromPasswordResetToken(request.getToken());
            userService.updatePassword(username, request.getNewPassword());
            User user = userService.findByUsername(username);

            // Send confirmation email
            emailService.sendPasswordChangedEmail(user.getEmail());

            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
