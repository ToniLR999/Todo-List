package com.tonilr.ToDoList.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    // No incluimos password por seguridad
}
