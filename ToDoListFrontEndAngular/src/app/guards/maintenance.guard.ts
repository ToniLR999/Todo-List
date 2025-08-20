import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { ScheduleService } from '../services/schedule.service';

@Injectable({
  providedIn: 'root'
})
export class MaintenanceGuard {
  constructor(
    private scheduleService: ScheduleService,
    private router: Router
  ) {}

  /**
   * Determina si una ruta puede ser activada basándose en el estado del horario.
   * @returns true si la aplicación está activa, false en caso contrario
   */
  canActivate(): Observable<boolean> {
    const isActive = this.scheduleService.isApplicationActive();
    
    if (!isActive) {
      // Si la aplicación no está activa, redirigir a mantenimiento
      this.router.navigate(['/maintenance']);
      return of(false);
    }
    
    // Si está activa, permitir acceso
    return of(true);
  }
}
