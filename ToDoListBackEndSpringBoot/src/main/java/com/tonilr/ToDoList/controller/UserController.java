package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.UserDTO;
import com.tonilr.ToDoList.dto.UserRegistrationDTO;
import com.tonilr.ToDoList.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

import com.tonilr.ToDoList.service.SecurityService;
import com.tonilr.ToDoList.dto.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200") // Para Angular
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private SecurityService securityService;

    @Autowired
    private DTOMapper dtoMapper;

    @Operation(summary = "Registrar un nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO userDTO) {
        try {
            var user = dtoMapper.toUser(userDTO);
            var newUser = userService.registerUser(user);
            return ResponseEntity.ok(dtoMapper.toUserDTO(newUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Obtener el current user")
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String username = securityService.getCurrentUsername();
            var user = userService.findByUsername(username);
            return ResponseEntity.ok(dtoMapper.toUserDTO(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar el perfil del usuario")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO userDetails) {
        try {
            String username = securityService.getCurrentUsername();
            var user = userService.findByUsername(username);
            user.setEmail(userDetails.getEmail());
            var updatedUser = userService.updateUser(user.getId(), user);
            return ResponseEntity.ok(dtoMapper.toUserDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
