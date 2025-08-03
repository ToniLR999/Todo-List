package com.tonilr.ToDoList.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.tonilr.ToDoList.model.Role;
import com.tonilr.ToDoList.model.User;

/**
 * DTO específico para el caché de usuarios.
 * Incluye información necesaria para autenticación sin problemas de serialización.
 */
public class UserCacheDTO {
    private Long id;
    private String username;
    private String email;
    private String password; // Necesario para autenticación
    private String timezone;
    private Set<String> roleNames; // Solo los nombres de los roles

    // Constructor por defecto
    public UserCacheDTO() {}

    // Constructor desde entidad User
    public UserCacheDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.timezone = user.getTimezone();
        this.roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    // Método para convertir de vuelta a User (sin roles)
    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setTimezone(this.timezone);
        return user;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Set<String> getRoleNames() { return roleNames; }
    public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }
}
