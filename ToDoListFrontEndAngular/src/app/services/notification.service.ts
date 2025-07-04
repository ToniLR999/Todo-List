/**
 * Notification service for managing user notification preferences.
 * Provides methods for retrieving and updating notification settings
 * with integration to the Spring Boot backend API.
 */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationPreferences } from '../models/notification-preferences.model';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';

  constructor(
    private http: HttpClient,
    private toastr: ToastrService
  ) {}

  /**
   * Retrieves current user's notification preferences.
   * @returns Observable of NotificationPreferences
   */
  getNotificationPreferences(): Observable<NotificationPreferences> {
    return this.http.get<NotificationPreferences>(`${this.apiUrl}/preferences`);
  }

  /**
   * Updates user's notification preferences.
   * @param preferences Updated notification preferences
   * @returns Observable of updated NotificationPreferences
   */
  updateNotificationPreferences(preferences: NotificationPreferences): Observable<NotificationPreferences> {
    return this.http.post<NotificationPreferences>(`${this.apiUrl}/preferences`, preferences).pipe(
      catchError(error => {
        if (error.status === 500) {
          this.toastr.error('Error al guardar las preferencias. Por favor, inténtalo de nuevo.');
        }
        return throwError(() => error);
      })
    );
  }
}