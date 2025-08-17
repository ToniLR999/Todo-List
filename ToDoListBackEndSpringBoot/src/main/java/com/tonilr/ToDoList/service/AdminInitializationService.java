package com.tonilr.ToDoList.service;

import com.tonilr.ToDoList.model.Role;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.UserRepository;
import com.tonilr.ToDoList.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para inicializar automáticamente el primer usuario administrador.
 * Se ejecuta al iniciar la aplicación si no existe ningún admin.
 */
@Service
@Slf4j
public class AdminInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Value("${app.admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.default.email:admin@todolist.com}")
    private String defaultAdminEmail;

    @Value("${app.admin.default.password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.admin.auto-create:true}")
    private boolean autoCreateAdmin;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!autoCreateAdmin) {
            log.info("Auto-creación de admin deshabilitada");
            return;
        }

        // Verificar si ya existe algún usuario con rol ADMIN
        boolean adminExists = userRepository.findAll().stream()
            .anyMatch(user -> user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName())));

        if (adminExists) {
            log.info("Ya existe un usuario administrador en el sistema");
            return;
        }

        // Crear el primer usuario administrador
        try {
            User adminUser = createDefaultAdmin();
            log.info("Usuario administrador creado automáticamente: {}", adminUser.getUsername());
            log.warn("⚠️ IMPORTANTE: Cambiar la contraseña del admin en producción!");
        } catch (Exception e) {
            log.error("Error creando usuario administrador por defecto: {}", e.getMessage());
        }
    }

    /**
     * Crea el usuario administrador por defecto.
     * @return Usuario administrador creado
     */
    private User createDefaultAdmin() {
        User adminUser = new User();
        adminUser.setUsername(defaultAdminUsername);
        adminUser.setEmail(defaultAdminEmail);
        adminUser.setPassword(passwordEncoder.encode(defaultAdminPassword));
        adminUser.setTimezone("UTC");

        // Asignar roles de administrador y usuario
        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> {
                Role newRole = new Role("ROLE_ADMIN", "Administrador");
                return roleRepository.save(newRole);
            });
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> {
                Role newRole = new Role("ROLE_USER", "Usuario");
                return roleRepository.save(newRole);
            });
        roles.add(adminRole);
        roles.add(userRole);
        adminUser.setRoles(roles);

        return userRepository.save(adminUser);
    }

    /**
     * Crea un usuario administrador personalizado.
     * @param username Nombre de usuario
     * @param email Email
     * @param password Contraseña (será hasheada)
     * @param timezone Zona horaria
     * @return Usuario administrador creado
     */
    @Transactional
    public User createCustomAdmin(String username, String email, String password, String timezone) {
        // Verificar que no exista ya
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        User adminUser = new User();
        adminUser.setUsername(username);
        adminUser.setEmail(email);
        adminUser.setPassword(passwordEncoder.encode(password));
        adminUser.setTimezone(timezone != null ? timezone : "UTC");

        // Asignar roles de administrador y usuario
        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> {
                Role newRole = new Role("ROLE_ADMIN", "Administrador");
                return roleRepository.save(newRole);
            });
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseGet(() -> {
                Role newRole = new Role("ROLE_USER", "Usuario");
                return roleRepository.save(newRole);
            });
        roles.add(adminRole);
        roles.add(userRole);
        adminUser.setRoles(roles);

        return userRepository.save(adminUser);
    }
}
