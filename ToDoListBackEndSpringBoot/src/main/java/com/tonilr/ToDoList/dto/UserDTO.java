package com.tonilr.ToDoList.dto;

import lombok.Data;

/**
 * Data Transfer Object for user operations.
 * Represents user data for API requests and responses, excluding
 * sensitive information like passwords for security purposes.
 * Uses Lombok @Data annotation for automatic getter/setter generation.
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String timezone;
    // No incluimos password por seguridad

    /**
     * Default constructor required for serialization.
     */
    public UserDTO() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
