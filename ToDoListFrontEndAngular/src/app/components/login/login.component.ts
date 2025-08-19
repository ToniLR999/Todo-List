/**
 * Login component for user authentication.
 * Provides a form for user login with validation and error handling.
 * Integrates with AuthService for JWT-based authentication.
 */
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  error: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  /**
   * Handles form submission for user login.
   */
  onSubmit(): void {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;
      this.authService.setAuthMethod('jwt');
      this.authService.login(username, password).subscribe({
        next: () => {
          setTimeout(() => this.router.navigate(['/tasks']), 100);
        },
        error: () => {
          this.error = 'Error de autenticación. Por favor, verifica tus credenciales.';
        }
      });
    }
  }

  goToRegister(): void { this.router.navigate(['/register']); }
  goToForgotPassword(): void { this.router.navigate(['/forgot-password']); }
}
