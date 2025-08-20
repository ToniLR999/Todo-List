# Sistema de Horario - ToDoList

## ¿Cómo Funciona?

Este sistema bloquea completamente el acceso a la aplicación cuando está fuera del horario de funcionamiento.

### Horario de Funcionamiento
- **Lunes a Viernes**: 10:00 AM - 7:00 PM (CET)
- **Fin de Semana**: Cerrado (Sábados y Domingos)
- **Zona Horaria**: Europe/Madrid

### ¿Qué Pasa Cuando Está Cerrado?

1. **No puedes navegar** a ninguna página excepto `/maintenance`
2. **No puedes hacer llamadas** al backend
3. **Eres redirigido automáticamente** a la página de mantenimiento
4. **No hay forma de bypass** - la lógica está hardcodeada

### ¿Qué Pasa Cuando Está Abierto?

1. **Acceso completo** a todas las funcionalidades
2. **Navegación libre** por toda la aplicación
3. **Llamadas al backend** funcionan normalmente
4. **Sin restricciones** de acceso

## Arquitectura

- **Frontend**: Maneja toda la lógica de horario
- **Backend**: Solo se enciende/apaga con GitHub Actions
- **Sin comunicación**: El frontend no necesita saber si el backend está activo

## Archivos Principales

- `schedule.service.ts` - Lógica del horario
- `maintenance.guard.ts` - Protección de rutas
- `schedule.interceptor.ts` - Bloqueo de HTTP
- `navigation-guard.service.ts` - Bloqueo de navegación

## Personalización

Para cambiar el horario, edita `schedule.service.ts`:

```typescript
private readonly BUSINESS_START_HOUR = 9;  // Cambiar hora de inicio
private readonly BUSINESS_END_HOUR = 18;   // Cambiar hora de cierre
private readonly TIMEZONE = 'America/New_York'; // Cambiar zona horaria
```

## Seguridad

- **No hay bypass posible** desde el cliente
- **Múltiples capas** de protección
- **Bloqueo total** fuera del horario
- **Redirección forzada** a mantenimiento
