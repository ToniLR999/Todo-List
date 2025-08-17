-- Crear el primer usuario administrador del sistema
-- V3__Create_First_Admin_User.sql

-- Insertar usuario admin por defecto
-- NOTA: La contraseña debe ser hasheada con BCrypt en producción
-- Este es solo un ejemplo, en producción usar un script de inicialización más seguro

INSERT INTO users (username, email, password, timezone) VALUES 
('admin', 'admin@todolist.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'UTC');

-- Asignar rol de administrador
INSERT INTO user_roles (user_id, role) VALUES 
((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
((SELECT id FROM users WHERE username = 'admin'), 'ROLE_USER');

-- NOTA IMPORTANTE: 
-- La contraseña hasheada arriba corresponde a 'admin123' 
-- En producción, cambiar inmediatamente esta contraseña
-- y usar un proceso más seguro para crear el primer admin
