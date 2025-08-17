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
import { SubscriptionManagerService } from '../../shared/subscription-manager.service';

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
    private router: Router,
    private subscriptionManager: SubscriptionManagerService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  /**
   * Handles form submission for user login.
   * Validates form, authenticates user, and navigates to tasks page on success.
   */
  onSubmit(): void {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;
      this.authService.setAuthMethod('jwt');
      this.subscriptionManager.subscribe(
        this.authService.login(username, password),
        (response) => {
          setTimeout(() => {
            this.router.navigate(['/tasks']);
          }, 100); // Pequeño delay para asegurar que el token se guarde
        },
        (error) => {
          // console.error('Error login:', error);
          this.error = 'Error de autenticación. Por favor, verifica tus credenciales.';
        }
      );
    }
  }

  /**
   * Navigates to the registration page.
   */
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  /**
   * Navigates to the forgot password page.
   */
  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
