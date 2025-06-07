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

@Service
@Slf4j
public class GlobalReminderService {
    @Autowired
    private NotificationPreferencesRepository notificationPreferencesRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private EmailService emailService;

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
                    log.info("Enviando recordatorios de tareas vencidas...");
                    sendOverdueReminders(pref);
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

    private void sendDueDateReminders(NotificationPreferences preferences) {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.info("Hora actual backend: {}", now);

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

            LocalDateTime reminderTime = now.plus(reminderDuration);

            // Tareas vencidas
            List<Task> overdueTasks = taskRepository.findByAssignedToAndDueDateBeforeAndCompletedFalse(
                preferences.getUser(), now
            );

            // Tareas próximas a vencer
            List<Task> upcomingTasks = taskRepository.findByAssignedToAndDueDateBetweenAndCompletedFalse(
                preferences.getUser(), now, reminderTime
            );

            // Unir ambas listas, evitando duplicados
            List<Task> allTasks = new java.util.ArrayList<>(overdueTasks);
            for (Task t : upcomingTasks) {
                if (!allTasks.contains(t)) {
                    allTasks.add(t);
                }
            }

            // Log de depuración
            log.info("Tareas a notificar (vencidas + próximas): {}", allTasks.size());
            for (Task t : allTasks) {
                log.info("Tarea: {} - Fecha límite: {}", t.getTitle(), t.getDueDate());
            }

            if (!allTasks.isEmpty()) {
                emailService.sendTaskReminderEmail(
                    preferences.getEmail(),
                    "Recordatorio: Tareas vencidas o próximas a vencer",
                    allTasks,
                    preferences.getUser()
                );
            } else {
                log.info("No hay tareas para enviar recordatorio");
            }
        } catch (Exception e) {
            log.error("Error en sendDueDateReminders: ", e);
        }
    }

    private void sendDailySummary(NotificationPreferences preferences) {
        try {
            log.info("Enviando resumen diario a: {}", preferences.getEmail());
            LocalDate today = LocalDate.now();
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

    private void sendWeeklySummary(NotificationPreferences preferences) {
        try {
            log.info("Enviando resumen semanal a: {}", preferences.getEmail());
            LocalDate today = LocalDate.now();
            // Suponiendo que la semana empieza el lunes
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
            LocalDate endOfWeek = startOfWeek.plusDays(7);

            List<Task> tasks = taskRepository.findByAssignedToAndDueDateBetweenAndCompletedFalse(
                preferences.getUser(),
                startOfWeek.atStartOfDay(),
                endOfWeek.atStartOfDay()
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

    private void sendOverdueReminders(NotificationPreferences preferences) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Task> overdueTasks = taskRepository.findByAssignedToAndDueDateBeforeAndCompletedFalse(
                preferences.getUser(), now
            );
            log.info("Tareas vencidas encontradas: {}", overdueTasks.size());

            if (!overdueTasks.isEmpty()) {
                emailService.sendTaskReminderEmail(
                    preferences.getEmail(),
                    "Aviso: Tareas vencidas",
                    overdueTasks,    
                    preferences.getUser()
                );
            }
        } catch (Exception e) {
            log.error("Error en sendOverdueReminders: ", e);
        }
    }
}
