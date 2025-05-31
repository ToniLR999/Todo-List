import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ToastrModule } from 'ngx-toastr';
import { NotificationPreferences } from '../../models/notification-preferences.model';
import { NotificationService } from 'src/app/services/notification.service';

@Component({
  selector: 'app-notification-preferences',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ToastrModule
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
      dailyReminders: [true],
      weeklySummary: [true]
    });
  }

  ngOnInit() {
    this.loadPreferences();
  }

  loadPreferences() {
    this.notificationService.getNotificationPreferences().subscribe({
      next: (preferences: NotificationPreferences) => {
        this.preferencesForm.patchValue(preferences);
      },
      error: (_: any) => {
        this.toastr.error('Error al cargar las preferencias', 'Error');
      }
    });
  }

  onSubmit() {
    if (this.preferencesForm.valid) {
      this.isSubmitting = true;
      this.notificationService.updateNotificationPreferences(this.preferencesForm.value).subscribe({
        next: () => {
          this.toastr.success('Preferencias guardadas correctamente', 'Éxito');
          this.isSubmitting = false;
        },
        error: (_: any) => {
          this.toastr.error('Error al guardar las preferencias', 'Error');
          this.isSubmitting = false;
        }
      });
    }
  }
}
