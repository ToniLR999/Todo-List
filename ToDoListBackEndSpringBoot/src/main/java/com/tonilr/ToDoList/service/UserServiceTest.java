package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.model.Role;
import com.tonilr.ToDoList.repository.UserRepository;
import com.tonilr.ToDoList.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    public UserServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.registerUser(user);

        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        assertTrue(result.getRoles().contains(role));
    }

    @Test
    void registerUser_usernameExists_throwsException() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(user);
        });

        assertEquals("El nombre de usuario ya existe", exception.getMessage());
    }
}
