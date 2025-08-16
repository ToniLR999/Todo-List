import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MaintenanceGuard {
  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Determines if a route can be activated based on maintenance status.
   * @returns true if application is active, false otherwise
   */
  canActivate(): Observable<boolean> {
    return this.http.get(`${environment.apiUrl}/api/app-status/status`)
      .pipe(
        map((response: any) => {
          const isActive = response.status === 'UP' && response.scheduleStatus === 'ACTIVO';
          
          if (!isActive) {
            // Si la aplicación no está activa, redirigir a mantenimiento
            this.router.navigate(['/maintenance']);
            return false;
          }
          
          // Si está activa, permitir acceso
          return true;
        }),
        catchError(() => {
          // En caso de error, permitir acceso (fallback)
          return of(true);
        })
      );
  }
}
