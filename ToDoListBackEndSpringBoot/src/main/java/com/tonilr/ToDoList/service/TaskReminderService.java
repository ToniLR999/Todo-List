package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.dto.TaskReminderDTO;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.model.TaskReminder;
import com.tonilr.ToDoList.repository.TaskReminderRepository;
import com.tonilr.ToDoList.repository.TaskRepository;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskReminderService {
    @Autowired
    private TaskReminderRepository reminderRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private JavaMailSender mailSender;

    public TaskReminderDTO createReminder(TaskReminderDTO dto) {
        Task task = taskRepository.findById(dto.getTaskId())
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        TaskReminder reminder = new TaskReminder();
        reminder.setTask(task);
        reminder.setReminderTime(dto.getReminderTime());
        reminder.setReminderType(dto.getReminderType());
        reminder.setSent(false);

        reminder = reminderRepository.save(reminder);
        return convertToDTO(reminder);
    }

    @Scheduled(cron = "0 * * * * *")  // Cada minuto
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<TaskReminder> reminders = reminderRepository
            .findByReminderTimeBeforeAndIsSentFalse(now);
        
        reminders.forEach(reminder -> {
            try {
                sendReminderEmail(reminder);
                reminder.setSent(true);
                reminderRepository.save(reminder);
            } catch (Exception e) {
                log.error("Error al enviar recordatorio: {}", e.getMessage(), e);
            }
        });
    }

    private void sendReminderEmail(TaskReminder reminder) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(reminder.getTask().getUser().getEmail());
        message.setSubject("Recordatorio de tarea: " + reminder.getTask().getTitle());
        
        String content = String.format(
            "Tienes una tarea pendiente:\n\n" +
            "Título: %s\n" +
            "Descripción: %s\n" +
            "Prioridad: %s\n" +
            "Fecha límite: %s\n",
            reminder.getTask().getTitle(),
            reminder.getTask().getDescription(),
            reminder.getTask().getPriority(),
            reminder.getTask().getDueDate()
        );
        
        message.setText(content);
        mailSender.send(message);
    }

    private TaskReminderDTO convertToDTO(TaskReminder reminder) {
        TaskReminderDTO dto = new TaskReminderDTO();
        dto.setId(reminder.getId());
        dto.setTaskId(reminder.getTask().getId());
        dto.setReminderTime(reminder.getReminderTime());
        dto.setReminderType(reminder.getReminderType());
        dto.setSent(reminder.isSent());
        return dto;
    }

    public List<TaskReminderDTO> getTaskReminders(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        return reminderRepository.findByTaskAndIsSentFalse(task)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public void deleteReminder(Long reminderId) {
        reminderRepository.deleteById(reminderId);
    }
}
