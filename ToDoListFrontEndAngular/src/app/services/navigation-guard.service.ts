import { Injectable } from '@angular/core';
import { Router, NavigationStart, NavigationEnd, NavigationCancel, NavigationError } from '@angular/router';
import { filter } from 'rxjs/operators';
import { ScheduleService } from './schedule.service';

@Injectable({
  providedIn: 'root'
})
export class NavigationGuardService {

  constructor(
    private router: Router,
    private scheduleService: ScheduleService
  ) {
    this.setupNavigationGuard();
  }

  /**
   * Configura el guard de navegación para bloquear acceso fuera del horario
   */
  private setupNavigationGuard() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    ).subscribe((event: NavigationStart) => {
      // Verificar si la aplicación está activa antes de permitir la navegación
      if (!this.scheduleService.isApplicationActive()) {
        // Si no está activa y no va a la página de mantenimiento, redirigir
        if (event.url !== '/maintenance') {
          console.log('Navegación bloqueada fuera del horario de funcionamiento');
          this.router.navigate(['/maintenance']);
        }
      }
    });
  }

  /**
   * Verifica si se puede navegar a una URL específica
   */
  canNavigateTo(url: string): boolean {
    // Siempre permitir acceso a la página de mantenimiento
    if (url === '/maintenance') {
      return true;
    }

    // Solo permitir navegación si la aplicación está activa
    return this.scheduleService.isApplicationActive();
  }

  /**
   * Fuerza la redirección a mantenimiento si está fuera del horario
   */
  forceMaintenanceRedirect(): void {
    if (!this.scheduleService.isApplicationActive()) {
      this.router.navigate(['/maintenance']);
    }
  }
}
