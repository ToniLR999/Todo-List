# 📋 ToDo List - Task Manager

A comprehensive task management application developed with **Angular 18** and **Spring Boot 3**, allowing users to efficiently organize and manage their tasks.

## 🌐 Live Demo

**🔗 [View application on Netlify](https://todolist-tonilr.netlify.app/)**

## ✨ Key Features

### 🎯 Task Management
- ✅ Create, edit and delete tasks
- 📅 Set due dates
- 🏷️ Assign priorities (High, Medium, Low)
- 📝 Detailed descriptions
- ✅ Mark tasks as completed

### 📚 Organization
- 📋 Create custom task lists
- 🏷️ Categorize tasks by lists
- 🔍 Advanced search and filters
- 📊 Progress statistics

### 🔔 Notification System
- 📧 Email notifications
- ⏰ Due date reminders
- 📅 Daily and weekly summaries
- ⚙️ Customizable preferences

### 🔐 Security
- 👤 JWT authentication
- 🔒 Secure registration and login
- 🛡️ Route protection
- 🔄 Password recovery

## 🛠️ Technologies Used

### Frontend
- **Angular 18** - Main framework
- **TypeScript** - Programming language
- **Bootstrap 5** - CSS framework
- **RxJS** - Reactive programming
- **ngx-toastr** - Notifications

### Backend
- **Spring Boot 3.2.3** - Java framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **MySQL** - Database
- **Redis** - Cache and sessions
- **JWT** - Authentication tokens

### DevOps
- **Netlify** - Frontend deployment
- **Railway** - Backend deployment
- **GitHub** - Version control

## 🚀 Installation and Setup

### Prerequisites
- Node.js 20.19.0 or higher
- Java 17 or higher
- MySQL 8.0 or higher
- Redis (optional for caching)

### Frontend (Angular)

```bash
# Clone the repository
git clone https://github.com/your-username/todo-list.git
cd todo-list/ToDoListFrontEndAngular

# Install dependencies
npm install

# Configure environment variables
# Create file src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};

# Run in development mode
npm start

# Build for production
npm run build
```

### Backend (Spring Boot)

```bash
# Navigate to backend directory
cd ../ToDoListBackEndSpringBoot

# Configure MySQL database
# Create file src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/todolist
spring.datasource.username=your_username
spring.datasource.password=your_password

# Run the application
./mvnw spring-boot:run
```

## 📁 Project Structure

```
Todo-List/
├── ToDoListFrontEndAngular/     # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/      # Angular Components
│   │   │   ├── services/        # Services
│   │   │   ├── models/          # TypeScript Interfaces
│   │   │   └── guards/          # Authentication Guards
│   │   └── environments/        # Environment configurations
│   └── package.json
├── ToDoListBackEndSpringBoot/   # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/tonilr/ToDoList/
│   │       ├── Controller/      # REST Controllers
│   │       ├── Service/         # Business Logic
│   │       ├── Repository/      # Data Access
│   │       ├── Model/           # JPA Entities
│   │       └── Security/        # Security Configuration
│   └── pom.xml
└── README.md
```

## 🔧 Database Configuration

```sql
-- Create database
CREATE DATABASE todolist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tables will be created automatically with JPA
```

## 🌍 Environment Variables

### Frontend (.env)
```env
API_URL=http://localhost:8080
```

### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/todolist
spring.datasource.username=root
spring.datasource.password=your_password

# JWT
app.jwtSecret=your_jwt_secret
app.jwtExpirationInMs=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# Redis (optional)
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

## 📦 Deployment

### Frontend (Netlify)
1. Connect GitHub repository to Netlify
2. Configure build settings:
   - Build command: `npm run build`
   - Publish directory: `dist/todo-angular-app`
3. Configure environment variables in Netlify

### Backend (Railway)
1. Connect GitHub repository to Railway
2. Configure environment variables
3. Automatic deployment on each push

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## 👨‍💻 Author

**Antonio López Rodríguez**
- GitHub: [@tonilr](https://github.com/tonilr)
- LinkedIn: [Antonio López](https://www.linkedin.com/in/antonio-lopez-rodriguez/)

## 🙏 Acknowledgments

- [Angular](https://angular.io/) - Frontend framework
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [Bootstrap](https://getbootstrap.com/) - CSS framework
- [Netlify](https://www.netlify.com/) - Frontend hosting
- [Railway](https://railway.app/) - Backend hosting

---

⭐ **If you like this project, give it a star on GitHub!**
