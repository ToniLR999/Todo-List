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
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Restablecimiento de contraseña");
            
            String content = String.format(
                "Para restablecer tu contraseña, haz clic en el siguiente enlace: " +
                "%s/reset-password?token=%s", frontendUrl, token);
            
            helper.setText(content, true);
            mailSender.send(message);
            
            log.info("Email de restablecimiento enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error enviando email de restablecimiento: {}", e.getMessage());
            throw new RuntimeException("Error enviando email de restablecimiento");
        }
    }
}
