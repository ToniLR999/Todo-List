import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NotificationPreferences } from '../../models/notification-preferences.model';
import { NotificationService } from 'src/app/services/notification.service';
import { catchError, throwError } from 'rxjs';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-preferences',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './notification-preferences.component.html',
  styleUrl: './notification-preferences.component.css'
})
export class NotificationPreferencesComponent implements OnInit {
  preferencesForm: FormGroup;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private toastr: ToastrService
  ) {
    this.preferencesForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      notificationType: ['both'],
      globalReminders: this.fb.group({
        dueDateReminder: [true],
        dueDateReminderTime: ['1d'],
        followUpReminder: [true],
        followUpDays: ['3'],
        dailySummary: [true],
        dailySummaryTime: ['09:00'],
        weeklySummary: [true],
        weeklySummaryDay: ['monday'],
        weeklySummaryTime: ['10:00'],
        assignmentNotification: [true],
        minPriority: ['2'],
        weekendNotifications: [false]
      })
    });
  }

  ngOnInit() {
    this.showInfo('Cargando preferencias de notificación...');
    this.loadPreferences();
  }

  loadPreferences() {
    this.notificationService.getNotificationPreferences().subscribe({
      next: (preferences: NotificationPreferences) => {
        this.preferencesForm.patchValue(preferences);
      },
      error: (_: any) => {
        this.showError('Error al cargar las preferencias');
      }
    });
  }

  onSubmit() {
    if (this.preferencesForm.valid) {
      this.isSubmitting = true;
      this.showInfo('Enviando preferencias al backend...');
      this.notificationService.updateNotificationPreferences(this.preferencesForm.value).subscribe({
        next: () => {
          this.showSuccess('Preferencias guardadas correctamente');
          this.isSubmitting = false;
        },
        error: (err: any) => {
          this.showError('Error al guardar las preferencias: ' + (err?.message || err));
          this.isSubmitting = false;
        }
      });
    } else {
      this.showError('Formulario inválido. Revisa los campos.');
      this.preferencesForm.markAllAsTouched();
    }
  }

  // Para mensajes informativos
  showInfo(msg: string) {
    this.toastr.info(msg, 'Info');
  }

  // Para mensajes de éxito
  showSuccess(msg: string) {
    this.toastr.success(msg, 'Éxito');
  }

  // Para mensajes de error
  showError(msg: string) {
    this.toastr.error(msg, 'Error');
  }
}
