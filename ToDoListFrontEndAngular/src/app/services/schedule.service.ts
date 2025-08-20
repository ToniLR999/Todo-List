import { Injectable } from '@angular/core';

export interface ScheduleStatus {
  isActive: boolean;
  currentTime: Date;
  businessStartHour: number;
  businessEndHour: number;
  timezone: string;
  nextStartTime: Date;
  message: string;
  status: 'ACTIVE' | 'INACTIVE' | 'WEEKEND';
}

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private readonly BUSINESS_START_HOUR = 10; // 10:00 AM
  private readonly BUSINESS_END_HOUR = 19;   // 7:00 PM
  private readonly TIMEZONE = 'Europe/Madrid';

  constructor() {}

  /**
   * Verifica si la aplicación está activa según el horario hardcodeado
   */
  isApplicationActive(): boolean {
    const now = new Date();
    const currentHour = now.getHours();
    const currentDay = now.getDay(); // 0 = Domingo, 1 = Lunes, ..., 6 = Sábado
    
    // Fin de semana: siempre cerrado
    if (currentDay === 0 || currentDay === 6) {
      return false;
    }
    
    // Días laborables: solo activo entre 10:00 y 19:00
    return currentHour >= this.BUSINESS_START_HOUR && currentHour < this.BUSINESS_END_HOUR;
  }

  /**
   * Obtiene el estado actual del horario
   */
  getCurrentScheduleStatus(): ScheduleStatus {
    const now = new Date();
    const currentHour = now.getHours();
    const currentDay = now.getDay();
    
    // Verificar si es fin de semana
    if (currentDay === 0 || currentDay === 6) {
      return this.createWeekendStatus(now);
    }
    
    // Verificar si está dentro del horario de funcionamiento
    const isActive = currentHour >= this.BUSINESS_START_HOUR && currentHour < this.BUSINESS_END_HOUR;
    
    if (isActive) {
      return this.createActiveStatus(now);
    } else {
      return this.createInactiveStatus(now);
    }
  }

  /**
   * Crea el estado para cuando la aplicación está activa
   */
  private createActiveStatus(now: Date): ScheduleStatus {
    return {
      isActive: true,
      currentTime: now,
      businessStartHour: this.BUSINESS_START_HOUR,
      businessEndHour: this.BUSINESS_END_HOUR,
      timezone: this.TIMEZONE,
      nextStartTime: this.calculateNextStartTime(now),
      message: 'La aplicación está funcionando normalmente.',
      status: 'ACTIVE'
    };
  }

  /**
   * Crea el estado para cuando la aplicación está inactiva
   */
  private createInactiveStatus(now: Date): ScheduleStatus {
    const nextStart = this.calculateNextStartTime(now);
    let message = '';
    
    if (now.getHours() < this.BUSINESS_START_HOUR) {
      message = `La aplicación estará disponible hoy a las ${this.BUSINESS_START_HOUR}:00.`;
    } else {
      message = `La aplicación estará disponible mañana a las ${this.BUSINESS_START_HOUR}:00.`;
    }

    return {
      isActive: false,
      currentTime: now,
      businessStartHour: this.BUSINESS_START_HOUR,
      businessEndHour: this.BUSINESS_END_HOUR,
      timezone: this.TIMEZONE,
      nextStartTime: nextStart,
      message: message,
      status: 'INACTIVE'
    };
  }

  /**
   * Crea el estado para el fin de semana
   */
  private createWeekendStatus(now: Date): ScheduleStatus {
    const nextStart = this.calculateNextStartTime(now);
    
    return {
      isActive: false,
      currentTime: now,
      businessStartHour: this.BUSINESS_START_HOUR,
      businessEndHour: this.BUSINESS_END_HOUR,
      timezone: this.TIMEZONE,
      nextStartTime: nextStart,
      message: 'La aplicación está cerrada durante el fin de semana.',
      status: 'WEEKEND'
    };
  }

  /**
   * Calcula la próxima hora de inicio
   */
  private calculateNextStartTime(now: Date): Date {
    const nextStart = new Date(now);
    
    // Si es fin de semana, ir al próximo lunes
    if (now.getDay() === 0) { // Domingo
      nextStart.setDate(now.getDate() + 1); // Lunes
    } else if (now.getDay() === 6) { // Sábado
      nextStart.setDate(now.getDate() + 2); // Lunes
    } else if (now.getHours() >= this.BUSINESS_END_HOUR) {
      // Si es después del horario de trabajo, ir al próximo día
      nextStart.setDate(now.getDate() + 1);
    }
    
    nextStart.setHours(this.BUSINESS_START_HOUR, 0, 0, 0);
    return nextStart;
  }

  /**
   * Formatea la hora para mostrar
   */
  formatTime(date: Date): string {
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: this.TIMEZONE
    });
  }

  /**
   * Formatea la fecha para mostrar
   */
  formatDate(date: Date): string {
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      timeZone: this.TIMEZONE
    });
  }

  /**
   * Obtiene el tiempo restante hasta el próximo inicio
   */
  getTimeUntilNextStart(): string {
    const now = new Date();
    const nextStart = this.getCurrentScheduleStatus().nextStartTime;
    const diff = nextStart.getTime() - now.getTime();
    
    if (diff <= 0) return 'Disponible ahora';
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) {
      return `${days} día${days > 1 ? 's' : ''}, ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m`;
    } else {
      return `${minutes}m`;
    }
  }
}
