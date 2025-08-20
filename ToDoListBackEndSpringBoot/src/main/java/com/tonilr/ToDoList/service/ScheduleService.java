package com.tonilr.ToDoList.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime; 

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${app.schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Value("${app.timezone:Europe/Madrid}")
private String appZoneId;

    private LocalDateTime lastScheduleCheck;
    private boolean isApplicationActive = true;

    // Horarios de trabajo: Lunes a Viernes 10:00 - 19:00
    private static final LocalTime WORK_START = LocalTime.of(10, 0);
    private static final LocalTime WORK_END = LocalTime.of(19, 0);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (isProduction()) {
            checkSchedule();
        } else {
            isApplicationActive = true;
        }
    }

    @Scheduled(fixedRate = 60000) // Verificar cada minuto
    public void checkSchedule() {
        ZoneId zoneId = ZoneId.of(appZoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDateTime localTime = now.toLocalDateTime();

        // Solo verificar horarios en producción
        if (!isProduction() || !scheduleEnabled) {
            return;
        }

        DayOfWeek currentDay = localTime.getDayOfWeek();
        LocalTime currentTime = localTime.toLocalTime();

        boolean shouldBeActive = isWorkTime(currentDay, currentTime);
        
        if (shouldBeActive != isApplicationActive) {
            if (shouldBeActive) {
                isApplicationActive = true;
            } else {
                scheduleShutdown();
            }
        }

        lastScheduleCheck = localTime;
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
        // En producción, en lugar de apagar el proceso, marcamos la app como inactiva
        if (!isProduction()) {
            return;
        }

        // Marcar como inactiva inmediatamente
        isApplicationActive = false;
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

        ZoneId zoneId = ZoneId.of(appZoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDateTime localTime = now.toLocalDateTime();
        DayOfWeek currentDay = localTime.getDayOfWeek();
        LocalTime currentTime = localTime.toLocalTime();

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

        ZoneId zoneId = ZoneId.of(appZoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDateTime localTime = now.toLocalDateTime();
        DayOfWeek currentDay = localTime.getDayOfWeek();
        LocalTime currentTime = localTime.toLocalTime();

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

        ZoneId zoneId = ZoneId.of(appZoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDateTime localTime = now.toLocalDateTime();
        DayOfWeek currentDay = localTime.getDayOfWeek();
        LocalTime currentTime = localTime.toLocalTime();
        
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
