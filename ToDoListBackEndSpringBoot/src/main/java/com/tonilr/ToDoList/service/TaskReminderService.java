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

/**
 * Service class for managing task reminders.
 * Provides functionality to create, retrieve, and process task reminders
 * with scheduled email notifications for upcoming task deadlines.
 */
@Service
@Slf4j
public class TaskReminderService {
    @Autowired
    private TaskReminderRepository reminderRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Creates a new reminder for a specific task.
     * @param dto Reminder data containing task ID and reminder settings
     * @return Created reminder DTO
     */
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

    /**
     * Scheduled method that runs every minute to check and send pending reminders.
     * Processes all unsent reminders that are due and sends email notifications.
     */
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

    /**
     * Sends an email reminder for a specific task.
     * @param reminder Task reminder to send email for
     */
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

    /**
     * Converts TaskReminder entity to DTO.
     * @param reminder Entity to convert
     * @return Converted DTO
     */
    private TaskReminderDTO convertToDTO(TaskReminder reminder) {
        TaskReminderDTO dto = new TaskReminderDTO();
        dto.setId(reminder.getId());
        dto.setTaskId(reminder.getTask().getId());
        dto.setReminderTime(reminder.getReminderTime());
        dto.setReminderType(reminder.getReminderType());
        dto.setSent(reminder.isSent());
        return dto;
    }

    /**
     * Retrieves all active reminders for a specific task.
     * @param taskId ID of the task to get reminders for
     * @return List of reminder DTOs
     */
    public List<TaskReminderDTO> getTaskReminders(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        return reminderRepository.findByTaskAndIsSentFalse(task)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Deletes a specific reminder by its ID.
     * @param reminderId ID of the reminder to delete
     */
    public void deleteReminder(Long reminderId) {
        reminderRepository.deleteById(reminderId);
    }
}
