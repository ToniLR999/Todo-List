package com.tonilr.ToDoList.exception;

public enum ErrorCode {
    // Errores de autenticación (1000-1999)
    AUTHENTICATION_FAILED(1000, "Error de autenticación"),
    INVALID_CREDENTIALS(1001, "Credenciales inválidas"),
    SESSION_EXPIRED(1002, "La sesión ha expirado"),
    
    // Errores de autorización (2000-2999)
    UNAUTHORIZED_ACCESS(2000, "No tienes permiso para realizar esta acción"),
    INSUFFICIENT_PERMISSIONS(2001, "No tienes los permisos necesarios"),
    
    // Errores de validación (3000-3999)
    VALIDATION_ERROR(3000, "Error de validación en los datos de entrada"),
    INVALID_INPUT(3001, "Datos de entrada inválidos"),
    REQUIRED_FIELD(3002, "Campo obligatorio"),
    INVALID_FORMAT(3003, "Formato inválido"),
    
    // Errores de recursos (4000-4999)
    RESOURCE_NOT_FOUND(4000, "Recurso no encontrado"),
    DUPLICATE_RESOURCE(4001, "El recurso ya existe"),
    
    // Errores de negocio (5000-5999)
    BUSINESS_RULE_VIOLATION(5000, "Violación de regla de negocio"),
    INVALID_OPERATION(5001, "Operación no válida"),
    
    // Errores del sistema (9000-9999)
    INTERNAL_ERROR(9000, "Error interno del servidor"),
    SERVICE_UNAVAILABLE(9001, "Servicio no disponible");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
