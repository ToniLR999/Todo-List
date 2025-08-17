-- Migración para convertir roles de tabla a enum
-- V2__Update_User_Roles_To_Enum.sql

-- 1. Crear nueva tabla temporal con la nueva estructura
CREATE TABLE users_new (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    timezone VARCHAR(100)
);

-- 2. Crear tabla para roles del usuario (element collection)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users_new(id) ON DELETE CASCADE
);

-- 3. Copiar datos de usuarios existentes
INSERT INTO users_new (id, username, email, password, timezone)
SELECT id, username, email, password, timezone FROM users;

-- 4. Asignar rol ROLE_USER por defecto a todos los usuarios existentes
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER' FROM users_new;

-- 5. Si hay usuarios que ya tenían roles específicos, migrarlos
-- (Esto dependerá de cómo estaban almacenados los roles anteriormente)

-- 6. Eliminar tabla antigua
DROP TABLE IF EXISTS user_roles_old;
DROP TABLE IF EXISTS roles;
DROP TABLE users;

-- 7. Renombrar nueva tabla
RENAME TABLE users_new TO users;

-- 8. Recrear índices
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);
