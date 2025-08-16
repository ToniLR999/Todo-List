package com.tonilr.ToDoList.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${app.schedule.enabled:true}")
    private boolean scheduleEnabled;

    private LocalDateTime lastScheduleCheck;
    private boolean isApplicationActive = true;

    // Horarios de trabajo: Lunes a Viernes 10:00 - 19:00
    private static final LocalTime WORK_START = LocalTime.of(10, 0);
    private static final LocalTime WORK_END = LocalTime.of(19, 0);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (isProduction()) {
            log.info("🚀 Aplicación iniciada en PRODUCCIÓN - Verificando horario de trabajo...");
            checkSchedule();
        } else {
            log.info("🚀 Aplicación iniciada en DESARROLLO - Sin restricciones horarias");
            isApplicationActive = true;
        }
    }

    @Scheduled(fixedRate = 60000) // Verificar cada minuto
    public void checkSchedule() {
        // Solo verificar horarios en producción
        if (!isProduction() || !scheduleEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        boolean shouldBeActive = isWorkTime(currentDay, currentTime);
        
        if (shouldBeActive != isApplicationActive) {
            if (shouldBeActive) {
                log.info("🌅 Horario de trabajo iniciado - Aplicación activa");
                isApplicationActive = true;
            } else {
                log.info("🌙 Horario de trabajo terminado - Aplicación se apagará en 5 minutos");
                scheduleShutdown();
            }
        }

        lastScheduleCheck = now;
    }

    private boolean isWorkTime(DayOfWeek day, LocalTime time) {
        // Solo lunes a viernes
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        // Entre 10:00 y 19:00
        return !time.isBefore(WORK_START) && !time.isAfter(WORK_END);
    }

    private void scheduleShutdown() {
        // Solo apagar en producción
        if (!isProduction()) {
            log.info("🔄 Modo desarrollo: No se apagará la aplicación");
            return;
        }

        // Programar apagado en 5 minutos
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000); // 5 minutos
                log.info("🔄 Apagando aplicación por horario de trabajo...");
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public boolean isApplicationActive() {
        // En desarrollo, siempre activo
        if (!isProduction()) {
            return true;
        }
        return isApplicationActive;
    }

    public String getCurrentSchedule() {
        // En desarrollo, siempre activo
        if (!isProduction()) {
            return "Desarrollo local - Siempre activo";
        }

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        if (isWorkTime(currentDay, currentTime)) {
            LocalTime timeUntilEnd = WORK_END.minusHours(currentTime.getHour())
                .minusMinutes(currentTime.getMinute());
            return String.format("Activo hasta %s (%s restante)", 
                WORK_END.format(DateTimeFormatter.ofPattern("HH:mm")),
                formatTimeRemaining(timeUntilEnd));
        } else {
            if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
                return "Fin de semana - Cerrado";
            } else if (currentTime.isBefore(WORK_START)) {
                LocalTime timeUntilStart = WORK_START.minusHours(currentTime.getHour())
                    .minusMinutes(currentTime.getMinute());
                return String.format("Cerrado - Abre a las %s (%s)", 
                    WORK_START.format(DateTimeFormatter.ofPattern("HH:mm")),
                    formatTimeRemaining(timeUntilStart));
            } else {
                return "Cerrado - Abre mañana a las 10:00";
            }
        }
    }

    public String getNextStartTime() {
        // En desarrollo, siempre activo
        if (!isProduction()) {
            return "Siempre activo (desarrollo)";
        }

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        if (currentDay == DayOfWeek.FRIDAY && currentTime.isAfter(WORK_END)) {
            // Si es viernes después de las 19:00, el próximo inicio es lunes
            return "Lunes 10:00";
        } else if (currentDay == DayOfWeek.SATURDAY) {
            return "Lunes 10:00";
        } else if (currentDay == DayOfWeek.SUNDAY) {
            return "Lunes 10:00";
        } else if (currentTime.isBefore(WORK_START)) {
            return "Hoy 10:00";
        } else {
            return "Mañana 10:00";
        }
    }

    private String formatTimeRemaining(LocalTime time) {
        if (time.getHour() > 0) {
            return String.format("%dh %dm", time.getHour(), time.getMinute());
        } else {
            return String.format("%dm", time.getMinute());
        }
    }

    public String getScheduleStatus() {
        // En desarrollo, siempre activo
        if (!isProduction()) {
            return "ACTIVO";
        }

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();
        
        if (isWorkTime(currentDay, currentTime)) {
            return "ACTIVO";
        } else {
            return "INACTIVO";
        }
    }

    private boolean isProduction() {
        return "railway".equals(activeProfile) || "prod".equals(activeProfile);
    }
}
