package com.tonilr.ToDoList.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.model.NotificationPreferences;
import com.tonilr.ToDoList.model.Task;
import com.tonilr.ToDoList.repository.NotificationPreferencesRepository;
import com.tonilr.ToDoList.repository.TaskRepository;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service class for managing global reminder operations.
 * Provides scheduled functionality to send automated email reminders
 * including due date reminders, daily summaries, and weekly summaries
 * based on user notification preferences.
 */
@Service
@Slf4j
public class GlobalReminderService {
    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private EmailService emailService;

    /**
     * Scheduled method that runs every minute to check and send reminders.
     * Processes all user notification preferences and sends appropriate reminders
     * based on configured settings (due date, daily, weekly summaries).
     */
    @Scheduled(cron = "0 */1 * * * *")  // Cada minuto
    public void checkAndSendReminders() {
        log.info("=== INICIANDO VERIFICACIÓN DE RECORDATORIOS ===");
        try {
            List<NotificationPreferences> preferences = notificationPreferencesRepository.findAll();
            log.info("Número de preferencias encontradas: {}", preferences.size());
            
            if (preferences.isEmpty()) {
                log.warn("No se encontraron preferencias de notificación");
                return;
            }

            for (NotificationPreferences pref : preferences) {
                log.info("Procesando usuario: {}", pref.getUser().getUsername());
                log.info("Email configurado: {}", pref.getEmail());
                log.info("Recordatorios activos: daily={}, weekly={}, dueDate={}", 
                    pref.isDailySummary(), 
                    pref.isWeeklySummary(), 
                    pref.isDueDateReminder());

                if (pref.isDueDateReminder()) {
                    log.info("Enviando recordatorios de vencimiento...");
                    sendDueDateReminders(pref);
                }
                if (pref.isDailySummary()) {
                    LocalTime horaActual = LocalTime.now().withSecond(0).withNano(0);
                    LocalTime horaConfigurada = LocalTime.parse(pref.getDailySummaryTime());
                    if (horaActual.equals(horaConfigurada)) {
                        log.info("Enviando resumen diario...");
                        sendDailySummary(pref);
                    }
                }
                if (pref.isWeeklySummary()) {
                    LocalTime horaActual = LocalTime.now().withSecond(0).withNano(0);
                    LocalTime horaConfigurada = LocalTime.parse(pref.getWeeklySummaryTime());
                    String diaActual = LocalDate.now().getDayOfWeek().name().toLowerCase();
                    if (horaActual.equals(horaConfigurada) && diaActual.equals(pref.getWeeklySummaryDay())) {
                        log.info("Enviando resumen semanal...");
                        sendWeeklySummary(pref);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error en checkAndSendReminders: ", e);
        }
        log.info("=== FINALIZADA VERIFICACIÓN DE RECORDATORIOS ===");
    }

    /**
     * Sends due date reminders for tasks that are approaching their deadline.
     * @param preferences User notification preferences containing reminder settings
     */
    private void sendDueDateReminders(NotificationPreferences preferences) {
        try {
            ZoneId userZone = ZoneId.of(preferences.getUser().getTimezone());
            LocalDateTime now = LocalDateTime.now(userZone);
            log.info("Hora actual backend (zona usuario): {}", now);

            // Duración configurada (ej: "1d", "1h")
            String durationStr = preferences.getDueDateReminderTime();
            Duration reminderDuration;
            if (durationStr.endsWith("d")) {
                int days = Integer.parseInt(durationStr.replace("d", ""));
                reminderDuration = Duration.ofDays(days);
            } else if (durationStr.endsWith("h")) {
                int hours = Integer.parseInt(durationStr.replace("h", ""));
                reminderDuration = Duration.ofHours(hours);
            } else {
                log.error("Formato de duración no válido: {}", durationStr);
                return;
            }

            LocalDateTime reminderStart = now.minus(reminderDuration);

            log.warn("Parámetros enviados al repo:");
            log.warn("Usuario: {}", preferences.getUser().getId());
            log.warn("Start: {}", reminderStart);
            log.warn("End: {}", now);

            log.warn("Buscando tareas próximas entre {} y {}", reminderStart, now);

            List<Task> allUserTasks = taskRepository.findByAssignedToAndCompletedFalse(preferences.getUser());
            log.warn("Tareas del usuario {} (no completadas): {}", preferences.getUser().getId(), allUserTasks.size());
            for (Task t : allUserTasks) {
                log.warn("Tarea BD - ID: {}, Título: {}, due_date: {}, assigned_to_id: {}", 
                    t.getId(), t.getTitle(), t.getDueDate(), 
                    t.getAssignedTo() != null ? t.getAssignedTo().getId() : null);
            }

            List<Task> upcomingTasks = taskRepository.findByAssignedToAndDueDateBetweenAndCompletedFalse(
                preferences.getUser(), reminderStart, now
            );
            if (upcomingTasks.isEmpty()) {
                log.warn("El repositorio NO ha devuelto ninguna tarea próxima. Revisa los parámetros enviados y las fechas en la BD.");
            } else {
                log.warn("Tareas próximas encontradas: {}", upcomingTasks.size());
                upcomingTasks.forEach(task ->
                    log.warn("Tarea encontrada - ID: {}, Fecha en BD: {}, Asignado a: {}", 
                        task.getId(), task.getDueDate(), task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                );
            }

            if (!upcomingTasks.isEmpty()) {
                emailService.sendTaskReminderEmail(
                    preferences.getEmail(),
                    "Recordatorio: Tareas próximas a vencer",
                    upcomingTasks,
                    preferences.getUser()
                );
            } else {
                log.warn("No hay tareas para enviar recordatorio");
            }
        } catch (Exception e) {
            log.error("Error en sendDueDateReminders: ", e);
        }
    }

    /**
     * Sends daily summary of pending tasks for the current day.
     * @param preferences User notification preferences
     */
    private void sendDailySummary(NotificationPreferences preferences) {
        try {
            log.info("Enviando resumen diario a: {}", preferences.getEmail());
            ZoneId userZone = ZoneId.of(preferences.getUser().getTimezone());
            LocalDate today = LocalDate.now(userZone);
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Task> tasks = taskRepository.findByAssignedToAndDueDateBetweenAndCompletedFalse(
                preferences.getUser(),
                startOfDay,
                endOfDay
            );

            log.info("Tareas encontradas para el resumen diario: {}", tasks.size());

            if (!tasks.isEmpty()) {
                emailService.sendTaskReminderEmail(
                    preferences.getEmail(),
                    "Resumen diario de tareas pendientes",
                    tasks,
                    preferences.getUser()
                );
            }
        } catch (Exception e) {
            log.error("Error en sendDailySummary: ", e);
        }
    }

    /**
     * Sends weekly summary of pending tasks for the current week.
     * @param preferences User notification preferences
     */
    private void sendWeeklySummary(NotificationPreferences preferences) {
        try {
            log.info("Enviando resumen semanal a: {}", preferences.getEmail());
            ZoneId userZone = ZoneId.of(preferences.getUser().getTimezone());
            LocalDate today = LocalDate.now(userZone);
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(7);

            LocalDateTime startOfWeekDateTime = startOfWeek.atStartOfDay();
            LocalDateTime endOfWeekDateTime = endOfWeek.atStartOfDay();

            List<Task> tasks = taskRepository.findByAssignedToAndDueDateBetweenAndCompletedFalse(
                preferences.getUser(),
                startOfWeekDateTime,
                endOfWeekDateTime
            );

            log.info("Tareas encontradas para el resumen semanal: {}", tasks.size());

            if (!tasks.isEmpty()) {
                emailService.sendTaskReminderEmail(
                    preferences.getEmail(),
                    "Resumen semanal de tareas pendientes",
                    tasks,
                    preferences.getUser()
                );
            }
        } catch (Exception e) {
            log.error("Error en sendWeeklySummary: ", e);
        }
    }

    /**
     * Truncates a Date object to seconds precision by removing milliseconds.
     * @param date Date to truncate
     * @return Truncated Date object
     */
    private Date truncateToSeconds(Date date) {
        long time = date.getTime();
        return new Date((time / 1000) * 1000);
    }

}
