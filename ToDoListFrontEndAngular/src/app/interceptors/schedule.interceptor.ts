import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ScheduleService } from '../services/schedule.service';

@Injectable()
export class ScheduleInterceptor implements HttpInterceptor {

  constructor(
    private scheduleService: ScheduleService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Verificar si la aplicación está activa según el horario
    if (!this.scheduleService.isApplicationActive()) {
      // Si no está activa, redirigir a mantenimiento y bloquear la llamada
      this.router.navigate(['/maintenance']);
      return throwError(() => new HttpErrorResponse({
        error: 'Application is outside business hours',
        status: 503,
        statusText: 'Service Unavailable'
      }));
    }

    // Si está activa, continuar con la llamada normal
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si hay un error de red y la aplicación no está activa, redirigir a mantenimiento
        if (error.status === 0 || error.status === 503) {
          if (!this.scheduleService.isApplicationActive()) {
            this.router.navigate(['/maintenance']);
          }
        }
        return throwError(() => error);
      })
    );
  }
}
