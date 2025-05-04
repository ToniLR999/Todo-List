package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.service.UserService;
import com.tonilr.ToDoList.service.SecurityService;
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String username = securityService.getCurrentUsername();
            User user = userService.findByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User userDetails) {
        try {
            String username = securityService.getCurrentUsername();
            User user = userService.findByUsername(username);
            User updatedUser = userService.updateUser(user.getId(), userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
