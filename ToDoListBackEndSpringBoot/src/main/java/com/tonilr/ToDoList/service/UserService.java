package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.exception.BadRequestException;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.UserRepository;
import com.tonilr.ToDoList.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @CacheEvict(value = "users", key = "#user.username")
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }
        
        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Asignar rol por defecto
        roleRepository.findByName("ROLE_USER")
            .ifPresent(role -> user.getRoles().add(role));
            
        User savedUser = userRepository.save(user);
        
        emailService.sendSimpleEmail(
            savedUser.getEmail(),
            "¡Bienvenido a ToDoList!",
            "Hola " + savedUser.getUsername() + ", tu cuenta ha sido creada correctamente."
        );
        

        auditLogService.logAction(savedUser, "REGISTRO_USUARIO", "Usuario registrado: " + savedUser.getUsername());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @CacheEvict(value = "users", key = "#userDetails.username")
    @Transactional
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
        user.setEmail(userDetails.getEmail());
        // No actualizamos username ni password aquí
        
        User updatedUser = userRepository.save(user);
        auditLogService.logAction(updatedUser, "ACTUALIZAR_USUARIO", "Usuario actualizado: " + updatedUser.getUsername());
        return updatedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updatePassword(String username, String newPassword) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
