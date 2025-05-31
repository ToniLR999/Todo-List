import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  error: string = '';
  success: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    public router: Router
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotPasswordForm.valid) {
      this.isLoading = true;
      this.error = '';
      this.success = '';
      this.forgotPasswordForm.disable();

      this.authService.forgotPassword(this.forgotPasswordForm.value.email)
        .subscribe({
          next: (response) => {
            console.log('Respuesta del servidor:', response); // Debug
            this.success = 'Se han enviado las instrucciones a tu correo electrónico';
            this.error = '';
            this.forgotPasswordForm.reset();
            this.forgotPasswordForm.enable();
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Error completo:', err); // Debug
            this.error = 'Error al procesar la solicitud';
            this.success = '';
            this.forgotPasswordForm.enable();
            this.isLoading = false;
          }
        });
    }
  }
}
