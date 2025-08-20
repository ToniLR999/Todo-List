# Sistema de Horario de Funcionamiento - Frontend Hardcodeado

## Descripción

Este sistema maneja automáticamente el horario de funcionamiento de la aplicación ToDoList **completamente en el frontend**, redirigiendo a los usuarios a la página de mantenimiento cuando la aplicación está fuera del horario de funcionamiento. **No hay dependencias del backend** para esta funcionalidad.

## Características

- **Horario de funcionamiento**: Lunes a Viernes, 10:00 AM - 7:00 PM (CET)
- **Fin de semana**: Cerrado (Sábados y Domingos)
- **Zona horaria**: Europe/Madrid (CET/CEST)
- **Lógica hardcodeada**: Sin llamadas HTTP al backend
- **Bloqueo completo**: No permite acceso a ninguna funcionalidad fuera del horario
- **Redirección automática**: A la página de mantenimiento cuando está cerrado

## Arquitectura del Sistema

### 1. ScheduleService (`src/app/services/schedule.service.ts`)

Servicio principal que maneja toda la lógica de horario de forma hardcodeada:

- **Sin observables complejos**: Lógica directa y simple
- **Cálculo en tiempo real**: Cada vez que se consulta
- **Sin dependencias externas**: Funciona completamente offline

#### Métodos principales:

```typescript
// Verificar si la aplicación está activa
isApplicationActive(): boolean

// Obtener estado actual del horario
getCurrentScheduleStatus(): ScheduleStatus

// Formatear hora y fecha
formatTime(date: Date): string
formatDate(date: Date): string

// Tiempo restante hasta próxima apertura
getTimeUntilNextStart(): string
```

### 2. MaintenanceGuard (`src/app/guards/maintenance.guard.ts`)

Guard que protege las rutas basándose en el estado del horario:

- **Verificación síncrona**: Sin observables complejos
- **Redirección inmediata**: A `/maintenance` si está inactiva
- **Protección de rutas**: Todas las rutas protegidas

### 3. ScheduleInterceptor (`src/app/interceptors/schedule.interceptor.ts`)

Interceptor HTTP que bloquea todas las llamadas al backend cuando está fuera del horario:

- **Bloqueo de API**: No permite llamadas HTTP fuera del horario
- **Redirección automática**: A la página de mantenimiento
- **Error 503**: Retorna error de servicio no disponible

### 4. NavigationGuardService (`src/app/services/navigation-guard.service.ts`)

Servicio que monitorea la navegación y bloquea cambios de ruta fuera del horario:

- **Monitoreo de navegación**: Intercepta todos los cambios de ruta
- **Bloqueo automático**: Redirige a mantenimiento si es necesario
- **Protección completa**: No permite navegar a otras páginas

### 5. MaintenanceInfoComponent (`src/app/components/maintenance-info/`)

Componente que muestra la información de mantenimiento:

- **Estado actual**: Muestra si está activa o inactiva
- **Información del horario**: Hora actual, próxima apertura
- **Cuenta regresiva**: Tiempo restante hasta la apertura

## Estados de la Aplicación

### ACTIVE
- La aplicación está funcionando normalmente
- Horario: Lunes a Viernes, 10:00 - 19:00
- Usuarios pueden acceder a todas las funcionalidades

### INACTIVE
- La aplicación está cerrada por horario
- Horario: Lunes a Viernes, antes de 10:00 o después de 19:00
- Usuarios son redirigidos a la página de mantenimiento

### WEEKEND
- La aplicación está cerrada durante el fin de semana
- Horario: Sábados y Domingos
- Usuarios son redirigidos a la página de mantenimiento

## Implementación

### 1. Configuración del Horario (Hardcodeada)

```typescript
private readonly BUSINESS_START_HOUR = 10; // 10:00 AM
private readonly BUSINESS_END_HOUR = 19;   // 7:00 PM
private readonly TIMEZONE = 'Europe/Madrid';
```

### 2. Cálculo del Estado

El servicio calcula el estado cada vez que se consulta:

```typescript
isApplicationActive(): boolean {
  const now = new Date();
  const currentHour = now.getHours();
  const currentDay = now.getDay();
  
  // Fin de semana: siempre cerrado
  if (currentDay === 0 || currentDay === 6) {
    return false;
  }
  
  // Días laborables: solo activo entre 10:00 y 19:00
  return currentHour >= this.BUSINESS_START_HOUR && 
         currentHour < this.BUSINESS_END_HOUR;
}
```

### 3. Protección de Rutas

Las rutas protegidas usan el `MaintenanceGuard`:

```typescript
{
  path: 'tasks',
  component: TaskListComponent,
  canActivate: [MaintenanceGuard, AuthGuard]
}
```

### 4. Bloqueo de HTTP

El interceptor bloquea todas las llamadas cuando está fuera del horario:

```typescript
intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
  if (!this.scheduleService.isApplicationActive()) {
    this.router.navigate(['/maintenance']);
    return throwError(() => new HttpErrorResponse({
      error: 'Application is outside business hours',
      status: 503,
      statusText: 'Service Unavailable'
    }));
  }
  return next.handle(request);
}
```

## Ventajas del Sistema Hardcodeado

1. **Sin dependencias del backend**: Funciona completamente offline
2. **Respuesta inmediata**: No hay latencia de red
3. **100% confiable**: No falla por problemas de conectividad
4. **Fácil mantenimiento**: Código simple y directo
5. **Seguridad total**: No permite bypass del horario

## Flujo de Funcionamiento

### Cuando la aplicación está ACTIVA:
1. ✅ Usuarios pueden navegar libremente
2. ✅ Todas las funcionalidades están disponibles
3. ✅ Las llamadas HTTP al backend funcionan normalmente
4. ✅ No hay restricciones de acceso

### Cuando la aplicación está INACTIVA:
1. 🚫 Usuarios son redirigidos a `/maintenance`
2. 🚫 No pueden navegar a otras páginas
3. 🚫 Todas las llamadas HTTP son bloqueadas
4. 🚫 Solo pueden ver la página de mantenimiento

## Personalización

Para cambiar el horario de funcionamiento, modifica las constantes en `ScheduleService`:

```typescript
private readonly BUSINESS_START_HOUR = 9;  // Cambiar a 9:00 AM
private readonly BUSINESS_END_HOUR = 18;   // Cambiar a 6:00 PM
private readonly TIMEZONE = 'America/New_York'; // Cambiar zona horaria
```

## Integración con GitHub Actions

El backend se enciende y apaga automáticamente con GitHub Actions:

- **Frontend**: Maneja la lógica de horario y bloqueo
- **Backend**: Solo se enciende/apaga según el horario configurado
- **Sin comunicación**: El frontend no necesita saber si el backend está activo

## Monitoreo

El sistema funciona completamente en el frontend:

1. **Consola del navegador**: Los cambios se registran en la consola
2. **Página de mantenimiento**: Muestra el estado actual
3. **Redirecciones automáticas**: Se ejecutan inmediatamente

## Troubleshooting

### La aplicación no redirige a mantenimiento:
1. Verificar que el `MaintenanceGuard` esté aplicado a las rutas
2. Comprobar que el `ScheduleService` esté funcionando
3. Revisar la consola del navegador para errores

### El horario no se actualiza:
1. Verificar que no haya errores en el cálculo del estado
2. Comprobar que la zona horaria esté configurada correctamente
3. Recargar la página para forzar el recálculo

### Problemas de zona horaria:
1. Verificar que `TIMEZONE` esté configurado correctamente
2. Comprobar que el navegador del usuario tenga la zona horaria correcta
3. El sistema usa la zona horaria del navegador del usuario

## Seguridad

- **No hay bypass**: La lógica está hardcodeada y no se puede modificar desde el cliente
- **Múltiples capas**: Guard, interceptor y servicio de navegación
- **Redirección forzada**: No permite acceso a funcionalidades fuera del horario
- **Bloqueo HTTP**: Previene cualquier comunicación con el backend
