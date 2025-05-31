import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationPreferences } from '../models/notification-preferences.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) {}

  getNotificationPreferences(): Observable<NotificationPreferences> {
    return this.http.get<NotificationPreferences>(`${this.apiUrl}/preferences`);
  }

  updateNotificationPreferences(preferences: NotificationPreferences): Observable<NotificationPreferences> {
    return this.http.post<NotificationPreferences>(`${this.apiUrl}/preferences`, preferences);
  }
}