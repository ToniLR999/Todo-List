package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

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
}
