package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.Role;
import com.tonilr.ToDoList.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para inicializar los roles por defecto del sistema.
 * Se ejecuta al iniciar la aplicación para asegurar que los roles básicos existan.
 */
@Service
@Slf4j
public class RoleInitializationService implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultRoles();
    }

    /**
     * Inicializa los roles por defecto del sistema.
     */
    private void initializeDefaultRoles() {
        try {
            // Crear ROLE_USER si no existe
            if (!roleRepository.findByName("ROLE_USER").isPresent()) {
                Role userRole = new Role("ROLE_USER", "Usuario");
                roleRepository.save(userRole);
                log.info("Rol ROLE_USER creado exitosamente");
            }

            // Crear ROLE_ADMIN si no existe
            if (!roleRepository.findByName("ROLE_ADMIN").isPresent()) {
                Role adminRole = new Role("ROLE_ADMIN", "Administrador");
                roleRepository.save(adminRole);
                log.info("Rol ROLE_ADMIN creado exitosamente");
            }

            // Crear ROLE_MODERATOR si no existe
            if (!roleRepository.findByName("ROLE_MODERATOR").isPresent()) {
                Role moderatorRole = new Role("ROLE_MODERATOR", "Moderador");
                roleRepository.save(moderatorRole);
                log.info("Rol ROLE_MODERATOR creado exitosamente");
            }

            log.info("Inicialización de roles completada exitosamente");
        } catch (Exception e) {
            log.error("Error durante la inicialización de roles: {}", e.getMessage(), e);
        }
    }
}
