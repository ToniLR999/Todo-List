package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.exception.BadRequestException;
import com.tonilr.ToDoList.exception.ResourceNotFoundException;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.UserRepository;
import com.tonilr.ToDoList.repository.RoleRepository;
import com.tonilr.ToDoList.dto.UserCacheDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;

/**
 * Service class for managing user operations.
 * Provides functionality for user registration, authentication, profile management,
 * and password operations with proper security measures and audit logging.
 */
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

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Registers a new user with password encryption and default role assignment.
     * @param user User entity to register
     * @return Registered user entity
     */
    @CacheEvict(value = {"users", "taskCounts", "userStats"}, allEntries = true)
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
        
        // Comentar temporalmente estas líneas para diagnosticar
        /*
        emailService.sendSimpleEmail(
            savedUser.getEmail(),
            "¡Bienvenido a ToDoList!",
            "Hola " + savedUser.getUsername() + ", tu cuenta ha sido creada correctamente."
        );
        
        auditLogService.logAction(savedUser, "REGISTRO_USUARIO", "Usuario registrado: " + savedUser.getUsername());
        */
        
        return savedUser;
    }

    /**
     * Retrieves a user by username with caching using DTO.
     * @param username Username to search for
     * @return User entity
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        // Primero intentar obtener del caché solo si Redis está disponible
        if (redisTemplate != null) {
            UserCacheDTO cachedUser = getUserFromCache(username);
            if (cachedUser != null) {
                return cachedUser.toUser();
            }
        }
        
        // Si no está en caché o Redis no está disponible, obtener de la base de datos
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Guardar en caché solo si Redis está disponible
        if (redisTemplate != null) {
            saveUserToCache(user);
        }
        
        return user;
    }

    /**
     * Obtiene usuario del caché usando DTO
     */
    private UserCacheDTO getUserFromCache(String username) {
        try {
            if (redisTemplate == null) return null;
            
            String cacheKey = "users::username_" + username;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof UserCacheDTO) {
                return (UserCacheDTO) cached;
            }
            return null;
        } catch (Exception e) {
            // Si hay error de deserialización, limpiar caché y continuar
            System.err.println("Error leyendo usuario del caché: " + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda usuario en caché usando DTO
     */
    private void saveUserToCache(User user) {
        try {
            if (redisTemplate == null) return;
            
            UserCacheDTO userCacheDTO = new UserCacheDTO(user);
            String cacheKey = "users::username_" + user.getUsername();
            redisTemplate.opsForValue().set(cacheKey, userCacheDTO, Duration.ofMinutes(30));
        } catch (Exception e) {
            // Log del error pero no fallar la operación
            System.err.println("Error guardando usuario en caché: " + e.getMessage());
        }
    }

    /**
     * Updates user profile information with cache invalidation.
     * @param userId ID of the user to update
     * @param userDetails Updated user information
     * @return Updated user entity
     */
    @CacheEvict(value = {"users", "taskCounts", "userStats"}, allEntries = true)
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

    /**
     * Retrieves a user by email address.
     * @param email Email to search for
     * @return User entity
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Updates a user's password with encryption.
     * @param username Username of the user
     * @param newPassword New password to set
     * @return Updated user entity
     */
    public User updatePassword(String username, String newPassword) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Saves a user entity to the database.
     * @param user User entity to save
     * @return Saved user entity
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Updates a user's timezone setting.
     * @param user User entity to update
     * @param timezone New timezone to set
     * @return Updated user entity
     */
    public User setTimezone(User user, String timezone) {
        user.setTimezone(timezone);
        return userRepository.save(user);
    }
}
