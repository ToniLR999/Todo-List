import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth-service.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
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

  onSubmit(): void {
    if (this.loginForm.valid) {
      const { username, password } = this.loginForm.value;
      this.authService.setAuthMethod('jwt');
      this.authService.login(username, password).subscribe({
        next: (response) => {
          console.log('Login exitoso, token:', response.token);
          if (response && response.token) {
            window.localStorage.setItem('token', response.token);
            console.log('Token guardado antes de navegar:', window.localStorage.getItem('token'));
            setTimeout(() => {
              this.router.navigate(['/tasks']);
            }, 100); // Pequeño delay para asegurar que el token se guarde
          }
        },
        error: (error) => {
          console.error('Error login:', error);
          this.error = 'Error de autenticación. Por favor, verifica tus credenciales.';
        }
      });
    }
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
