package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.User;
import com.tonilr.ToDoList.repository.TaskRepository;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import java.time.ZoneId;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Service class for handling email operations.
 * Provides functionality to send various types of emails including password reset,
 * task reminders, and user notifications with proper HTML formatting and sanitization.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TaskRepository taskRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Autowired
    private SanitizationService sanitizationService;
    
    /**
     * Sends a simple text email.
     * @param to Recipient email address
     * @param subject Email subject
     * @param text Email body text
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("TU_CORREO@gmail.com"); // Cambia por tu correo
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    
    /**
     * Sends a password reset email with HTML formatting and security token.
     * @param to Recipient email address
     * @param token Password reset token
     */
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(sanitizeEmail(to));
            helper.setSubject("Restablecimiento de Contraseña - ToDoList");
            helper.setFrom("noreply@todolist.com");
            
            // Sanitizar el contenido HTML
            String sanitizedContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #007bff;">Restablecimiento de Contraseña</h2>
                        <p>Hola,</p>
                        <p>Hemos recibido una solicitud para restablecer tu contraseña. Si no realizaste esta solicitud, puedes ignorar este correo.</p>
                        <p>Para restablecer tu contraseña, haz clic en el siguiente botón:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s/reset-password?token=%s" 
                               style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px;">
                                Restablecer Contraseña
                            </a>
                        </div>
                        <p>Este enlace expirará en 1 hora por razones de seguridad.</p>
                        <p>Saludos,<br>El equipo de ToDoList</p>
                    </div>
                </body>
                </html>
                """, frontendUrl, sanitizationService.sanitizeText(token));
            
            helper.setText(sanitizedContent, true);
            message.setHeader("X-Priority", "1"); // Alta prioridad
            message.setHeader("X-MSMail-Priority", "High");
            message.setHeader("Importance", "High");
            
            mailSender.send(message);
            log.info("Email de restablecimiento enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error enviando email de restablecimiento: {}", e.getMessage());
            throw new RuntimeException("Error enviando email de restablecimiento");
        }
    }
    
    /**
     * Sends a confirmation email when password is successfully changed.
     * @param to Recipient email address
     */
    public void sendPasswordChangedEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Contraseña Actualizada - ToDoList");
        message.setText("Tu contraseña ha sido actualizada exitosamente. " +
                       "Si no realizaste este cambio, por favor contacta con soporte inmediatamente.");
        mailSender.send(message);
    }
    
    /**
     * Sends a task reminder email with HTML formatting and user timezone support.
     * @param to Recipient email address
     * @param subject Email subject
     * @param tasks List of tasks to include in the reminder
     * @param user User for timezone conversion
     */
    public void sendTaskReminderEmail(String to, String subject, List<Task> tasks, User user) {
        try {
            log.info("Preparando email de recordatorio para: {}", to);
            log.info("Número de tareas a enviar: {}", tasks.size());
            log.info("Zona horaria del usuario: {}", user.getTimezone());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            String content = buildEmailContent(tasks, subject, user);
            helper.setText(content, true);
            
            log.info("Enviando email a: {}", to);
            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email: ", e);
            throw new RuntimeException("Error al enviar email", e);
        }
    }
    
    /**
     * Builds HTML email content for task reminders with user timezone support.
     * @param tasks List of tasks to include
     * @param subject Email subject
     * @param user User for timezone conversion
     * @return Formatted HTML content
     */
    private String buildEmailContent(List<Task> tasks, String subject, User user) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>");
        content.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>");
        content.append("<h2 style='color: #007bff;'>").append(subject).append("</h2>");

        // Usar la zona horaria del usuario
        ZoneId userZone = ZoneId.of(user.getTimezone());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        tasks.forEach(task -> {
            String fechaFormateada = "";
            if (task.getDueDate() != null) {
                // Convertir directamente a la zona horaria del usuario
                LocalDateTime localDateTime = task.getDueDate();
                fechaFormateada = formatter.format(localDateTime);
            }
            content.append("<div style='margin: 15px 0; padding: 10px; border-left: 4px solid #007bff;'>");
            content.append("<h3 style='margin: 0;'>").append(task.getTitle()).append("</h3>");
            content.append("<p>Fecha límite: ").append(fechaFormateada).append("</p>");
            content.append("<p>Prioridad: ").append(getPriorityLabel(task.getPriority())).append("</p>");
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                content.append("<p>").append(task.getDescription()).append("</p>");
            }
            content.append("</div>");
        });

        content.append("</div></body></html>");
        return content.toString();
    }
    
    /**
     * Converts priority number to human-readable label.
     * @param priority Priority number (1-3)
     * @return Priority label in Spanish
     */
    private String getPriorityLabel(int priority) {
        switch (priority) {
            case 1: return "Alta";
            case 2: return "Media";
            case 3: return "Baja";
            default: return "Desconocida";
        }
    }
    
    /**
     * Sanitizes email address by removing potentially dangerous characters.
     * @param email Email address to sanitize
     * @return Sanitized email address
     */
    private String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.replaceAll("[^a-zA-Z0-9@._-]", "");
    }
}
