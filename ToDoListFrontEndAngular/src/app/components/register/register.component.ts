/**
 * Registration component for new user account creation.
 * Provides a form for user registration with validation, password confirmation,
 * and timezone selection. Integrates with AuthService for account creation.
 */
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  error: string = '';
  timezones = [
    'Europe/Madrid',
    'Europe/London',
    'America/New_York',
    'America/Argentina/Buenos_Aires',
    'America/Mexico_City',
    'Asia/Tokyo',
    'Asia/Shanghai',
    'Europe/Paris',
    'Europe/Berlin',
    'UTC'
    // ...añade las que quieras
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      timezone: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  /**
   * Custom validator to ensure password and confirm password match.
   * @param g FormGroup to validate
   * @returns null if passwords match, error object otherwise
   */
  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  /**
   * Gets error message for form validation errors.
   * @param controlName Name of the form control
   * @returns Error message string
   */
  getErrorMessage(controlName: string): string {
    const control = this.registerForm.get(controlName);
    if (control?.hasError('required')) {
      return 'Este campo es obligatorio';
    }
    if (control?.hasError('email')) {
      return 'Email inválido';
    }
    if (control?.hasError('minlength')) {
      return 'Mínimo 6 caracteres';
    }
    if (controlName === 'confirmPassword' && this.registerForm.hasError('mismatch')) {
      return 'Las contraseñas no coinciden';
    }
    return '';
  }

  /**
   * Handles form submission for user registration.
   * Validates form, creates user account, and navigates to login page on success.
   */
  onSubmit(): void {
    if (this.registerForm.valid) {
      const { username, email, password } = this.registerForm.value;
      this.authService.register(username, email, password).subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: (error) => {
          if (error.status === 409) {
            this.error = 'El usuario o email ya existe';
          } else {
            this.error = 'Error en el registro. Por favor, intenta de nuevo.';
          }
        }
      });
    }
  }

  /**
   * Navigates to the login page.
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
