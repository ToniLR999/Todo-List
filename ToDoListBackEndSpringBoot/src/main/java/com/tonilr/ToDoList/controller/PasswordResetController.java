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

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class PasswordResetController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Operation(summary = "Solicitar restablecimiento de contraseña")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequestDTO request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            String token = tokenProvider.generatePasswordResetToken(user.getUsername());
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return ResponseEntity.ok().body(Map.of("message", "Email de restablecimiento enviado"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Restablecer contraseña")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO request) {
        try {
            String username = tokenProvider.getUsernameFromPasswordResetToken(request.getToken());
            userService.updatePassword(username, request.getNewPassword());
            return ResponseEntity.ok().body(Map.of("message", "Contraseña actualizada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
