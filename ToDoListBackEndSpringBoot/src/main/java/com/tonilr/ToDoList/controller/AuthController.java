package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.LoginDTO;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            // La autenticación real la maneja Spring Security
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error de autenticación");
        }
    }

    @Operation(summary = "Cerrar sesión")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // El logout real lo maneja Spring Security
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verificar autenticación")
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok().build();
    }
}
