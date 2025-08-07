# 📋 ToDo List - Gestor de Tareas

Una aplicación completa de gestión de tareas desarrollada con **Angular 18** y **Spring Boot 3**, que permite a los usuarios organizar y gestionar sus tareas de manera eficiente.

## 🌐 Demo en Vivo

**🔗 [Ver aplicación en Netlify](https://todolist-tonilr.netlify.app/)**

## ✨ Características Principales

### 🎯 Gestión de Tareas
- ✅ Crear, editar y eliminar tareas
- 📅 Establecer fechas límite
- 🏷️ Asignar prioridades (Alta, Media, Baja)
- 📝 Descripciones detalladas
- ✅ Marcar tareas como completadas

### 📚 Organización
- 📋 Crear listas de tareas personalizadas
- 🏷️ Categorizar tareas por listas
- 🔍 Búsqueda y filtros avanzados
- 📊 Estadísticas de progreso

### 🔔 Sistema de Notificaciones
- 📧 Notificaciones por email
- ⏰ Recordatorios de fechas límite
- 📅 Resúmenes diarios y semanales
- ⚙️ Preferencias personalizables

### 🔐 Seguridad
- 👤 Autenticación JWT
- 🔒 Registro e inicio de sesión seguro
- 🛡️ Protección de rutas
- 🔄 Recuperación de contraseña

## 🛠️ Tecnologías Utilizadas

### Frontend
- **Angular 18** - Framework principal
- **TypeScript** - Lenguaje de programación
- **Bootstrap 5** - Framework CSS
- **RxJS** - Programación reactiva
- **ngx-toastr** - Notificaciones

### Backend
- **Spring Boot 3.2.3** - Framework Java
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **MySQL** - Base de datos
- **Redis** - Caché y sesiones
- **JWT** - Tokens de autenticación

### DevOps
- **Netlify** - Despliegue frontend
- **Railway** - Despliegue backend
- **GitHub** - Control de versiones

## 🚀 Instalación y Configuración

### Prerrequisitos
- Node.js 20.19.0 o superior
- Java 17 o superior
- MySQL 8.0 o superior
- Redis (opcional para caché)

### Frontend (Angular)

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/todo-list.git
cd todo-list/ToDoListFrontEndAngular

# Instalar dependencias
npm install

# Configurar variables de entorno
# Crear archivo src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};

# Ejecutar en modo desarrollo
npm start

# Construir para producción
npm run build
```

### Backend (Spring Boot)

```bash
# Navegar al directorio del backend
cd ../ToDoListBackEndSpringBoot

# Configurar base de datos MySQL
# Crear archivo src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/todolist
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# Ejecutar la aplicación
./mvnw spring-boot:run
```

## 📁 Estructura del Proyecto

```
Todo-List/
├── ToDoListFrontEndAngular/     # Frontend Angular
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/      # Componentes Angular
│   │   │   ├── services/        # Servicios
│   │   │   ├── models/          # Interfaces TypeScript
│   │   │   └── guards/          # Guards de autenticación
│   │   └── environments/        # Configuraciones de entorno
│   └── package.json
├── ToDoListBackEndSpringBoot/   # Backend Spring Boot
│   ├── src/main/java/
│   │   └── com/tonilr/ToDoList/
│   │       ├── Controller/      # Controladores REST
│   │       ├── Service/         # Lógica de negocio
│   │       ├── Repository/      # Acceso a datos
│   │       ├── Model/           # Entidades JPA
│   │       └── Security/        # Configuración de seguridad
│   └── pom.xml
└── README.md
```

## 🔧 Configuración de Base de Datos

```sql
-- Crear base de datos
CREATE DATABASE todolist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Las tablas se crearán automáticamente con JPA
```

## 🌍 Variables de Entorno

### Frontend (.env)
```env
API_URL=http://localhost:8080
```

### Backend (application.properties)
```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/todolist
spring.datasource.username=root
spring.datasource.password=tu_password

# JWT
app.jwtSecret=tu_jwt_secret
app.jwtExpirationInMs=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_app_password

# Redis (opcional)
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

## 🧪 Testing

### Frontend
```bash
cd ToDoListFrontEndAngular
npm test
```

### Backend
```bash
cd ToDoListBackEndSpringBoot
./mvnw test
```

## 📦 Despliegue

### Frontend (Netlify)
1. Conectar repositorio GitHub a Netlify
2. Configurar build settings:
   - Build command: `npm run build`
   - Publish directory: `dist/todo-angular-app`
3. Configurar variables de entorno en Netlify

### Backend (Railway)
1. Conectar repositorio GitHub a Railway
2. Configurar variables de entorno
3. Deploy automático en cada push

## 🤝 Contribuir

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Autor

**Antonio López Rodríguez**
- GitHub: [@tonilr](https://github.com/tonilr)
- LinkedIn: [Antonio López](https://www.linkedin.com/in/antonio-lopez-rodriguez/)

## 🙏 Agradecimientos

- [Angular](https://angular.io/) - Framework frontend
- [Spring Boot](https://spring.io/projects/spring-boot) - Framework backend
- [Bootstrap](https://getbootstrap.com/) - Framework CSS
- [Netlify](https://www.netlify.com/) - Hosting frontend
- [Railway](https://railway.app/) - Hosting backend

---

⭐ **¡Si te gusta este proyecto, dale una estrella en GitHub!**
