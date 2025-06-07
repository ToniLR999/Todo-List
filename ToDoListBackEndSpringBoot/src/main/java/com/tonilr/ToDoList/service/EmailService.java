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
    
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("TU_CORREO@gmail.com"); // Cambia por tu correo
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
    
    public void sendPasswordResetEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Restablecimiento de contraseña - ToDoList");
            
            String content = String.format("""
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
                """, frontendUrl, token);
            
            helper.setText(content, true);
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
    
    public void sendPasswordChangedEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Contraseña Actualizada - ToDoList");
        message.setText("Tu contraseña ha sido actualizada exitosamente. " +
                       "Si no realizaste este cambio, por favor contacta con soporte inmediatamente.");
        mailSender.send(message);
    }
    
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
    
    private String getPriorityLabel(int priority) {
        switch (priority) {
            case 1: return "Alta";
            case 2: return "Media";
            case 3: return "Baja";
            default: return "Desconocida";
        }
    }
}
